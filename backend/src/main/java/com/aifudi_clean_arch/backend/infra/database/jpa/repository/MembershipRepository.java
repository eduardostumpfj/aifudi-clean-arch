package com.aifudi_clean_arch.backend.infra.database.jpa.repository;

import com.aifudi_clean_arch.backend.infra.database.jpa.entity.MembershipEntity;
import com.aifudi_clean_arch.backend.infra.database.mapper.MembershipMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface MembershipRepository extends JpaRepository<MembershipEntity, UUID> {

    public Optional<MembershipEntity> findByUser_IdAndRestaurant_Id(
            UUID userId,
            UUID restaurantId
    );

    List<MembershipEntity> findByRestaurant_Id(UUID restaurantId);

    @Modifying
    @Query("delete from MembershipEntity membership where membership.restaurant.id = :restaurantId")
    int deleteByRestaurantId(@Param("restaurantId") UUID restaurantId);

    Optional<MembershipEntity> findByUser_Id(UUID userId);
}
