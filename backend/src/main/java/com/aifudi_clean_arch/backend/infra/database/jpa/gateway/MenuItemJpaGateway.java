package com.aifudi_clean_arch.backend.infra.database.jpa.gateway;

import com.aifudi_clean_arch.backend.core.domain.MenuItem;
import com.aifudi_clean_arch.backend.core.exception.ErrorOnRepositoryAccess;
import com.aifudi_clean_arch.backend.core.exception.MenuItemNotFountException;
import com.aifudi_clean_arch.backend.core.gateway.MenuItemGateway;
import com.aifudi_clean_arch.backend.infra.database.jpa.repository.MenuItemRepository;
import com.aifudi_clean_arch.backend.infra.database.jpa.repository.RestaurantRepository;
import com.aifudi_clean_arch.backend.infra.database.mapper.MenuItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MenuItemJpaGateway implements MenuItemGateway {
    private final MenuItemRepository repository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemMapper mapper;

    @Override
    @Transactional
    public MenuItem save(MenuItem menuItem) {
        try {
            var restaurant = restaurantRepository.getReferenceById(menuItem.getRestaurantId());
            return mapper.map(repository.save(mapper.map(menuItem, restaurant)));
        } catch (DataAccessException ex) {
            throw new ErrorOnRepositoryAccess("Error saving menu item: " + menuItem.getId(), ex);
        }
    }

    @Override
    public Optional<MenuItem> getById(UUID id) {
        try {
            return repository.findById(id).map(mapper::map);
        } catch (DataAccessException ex) {
            throw new ErrorOnRepositoryAccess("Error retrieving menu item: " + id, ex);
        }
    }

    @Override
    @Transactional
    public boolean delete(UUID id) {
        try {
            return repository.deleteByIdReturningCount(id) > 0;
        } catch (DataAccessException ex) {
            throw new ErrorOnRepositoryAccess("Error deleting menu item: " + id, ex);
        }
    }

    @Override
    @Transactional
    public MenuItem update(MenuItem menuItem) {
        try {
            var current = repository.findById(menuItem.getId())
                    .orElseThrow(() -> new MenuItemNotFountException(menuItem.getId()));
            var restaurant = restaurantRepository.getReferenceById(menuItem.getRestaurantId());

            current.setName(menuItem.getName());
            current.setDescription(menuItem.getDescription());
            current.setPrice(menuItem.getPrice());
            current.setPicture(menuItem.getPicture());
            current.setHas_delivery(menuItem.isHasDelivery());
            current.setRestaurant(restaurant);

            return mapper.map(repository.save(current));
        } catch (DataAccessException ex) {
            throw new ErrorOnRepositoryAccess("Error updating menu item: " + menuItem.getId(), ex);
        }
    }
}
