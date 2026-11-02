package com.aifudi_clean_arch.backend.infra.database.jpa.gateway;

import com.aifudi_clean_arch.backend.core.domain.Restaurant;
import com.aifudi_clean_arch.backend.core.exception.ErrorOnRepositoryAccess;
import com.aifudi_clean_arch.backend.core.exception.RestaurantNotFoundException;
import com.aifudi_clean_arch.backend.core.gateway.RestaurantGateway;
import com.aifudi_clean_arch.backend.infra.database.jpa.entity.RestaurantEntity;
import com.aifudi_clean_arch.backend.infra.database.jpa.repository.RestaurantRepository;
import com.aifudi_clean_arch.backend.infra.database.jpa.repository.KitchenRepository;
import com.aifudi_clean_arch.backend.infra.database.jpa.repository.MembershipRepository;
import com.aifudi_clean_arch.backend.infra.database.jpa.repository.MenuItemRepository;
import com.aifudi_clean_arch.backend.infra.database.mapper.RestaurantMapper;
import com.aifudi_clean_arch.backend.infra.database.mapper.MembershipMapper;
import com.aifudi_clean_arch.backend.infra.database.mapper.MenuItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RestaurantJpaGateway implements RestaurantGateway {
    private final RestaurantRepository repository;
    private final KitchenRepository kitchenRepository;
    private final MembershipRepository membershipRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantMapper mapper;
    private final MembershipMapper membershipMapper;
    private final MenuItemMapper menuItemMapper;

    @Override
    public Optional<Restaurant> getRestaurantById(UUID id) {
        try {
            return repository.findById(id).map(this::toDomainWithCollections);
        } catch (DataAccessException ex) {
            throw new ErrorOnRepositoryAccess("Error retrieving restaurant: " + id, ex);
        }
    }

    @Override
    @Transactional
    public Restaurant save(Restaurant restaurant) {
        try {
            var entity = mapper.map(restaurant);
            entity.setKitchen(kitchenRepository.getReferenceById(restaurant.getKitchen().getId()));
            var saved = repository.save(entity);
            return new Restaurant(saved.getId(), saved.getName(), saved.getAddress(),
                    saved.getOpening_time(), restaurant.getKitchen(),
                    restaurant.getMembers(), restaurant.getMenuItems());
        } catch (DataAccessException ex) {
            throw new ErrorOnRepositoryAccess("Error saving restaurant: " + restaurant.getId(), ex);
        }
    }

    @Override
    @Transactional
    public boolean delete(UUID id) {
        try {
            membershipRepository.deleteByRestaurantId(id);
            return repository.deleteByIdReturningCount(id) > 0;
        } catch (DataAccessException ex) {
            throw new ErrorOnRepositoryAccess("Error deleting restaurant: " + id, ex);
        }
    }

    @Override
    @Transactional
    public Restaurant update(Restaurant restaurant) {
        try {
            var current = repository.findById(restaurant.getId())
                    .orElseThrow(() -> new RestaurantNotFoundException(restaurant.getId()));
            current.setName(restaurant.getName());
            current.setAddress(restaurant.getAddress());
            current.setOpening_time(restaurant.getOpeningTime());
            current.setKitchen(kitchenRepository.getReferenceById(restaurant.getKitchen().getId()));
            return toDomainWithCollections(repository.save(current));
        } catch (DataAccessException ex) {
            throw new ErrorOnRepositoryAccess("Error updating restaurant: " + restaurant.getId(), ex);
        }
    }

    private Restaurant toDomainWithCollections(RestaurantEntity entity) {
        var basic = mapper.map(entity);
        var members = membershipRepository.findByRestaurant_Id(entity.getId()).stream()
                .map(membershipMapper::map).collect(Collectors.toSet());
        var items = menuItemRepository.findByRestaurant_Id(entity.getId()).stream()
                .map(menuItemMapper::map).collect(Collectors.toSet());
        return new Restaurant(basic.getId(), basic.getName(), basic.getAddress(),
                basic.getOpeningTime(), basic.getKitchen(), members, items);
    }
}
