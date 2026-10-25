package com.aifudi_clean_arch.backend.infra.database.jpa.repository;

import com.aifudi_clean_arch.backend.infra.database.jpa.entity.MenuItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;
import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItemEntity, UUID> {
    List<MenuItemEntity> findByRestaurant_Id(UUID restaurantId);

    @Modifying
    @Query("delete from MenuItemEntity item where item.id = :id")
    int deleteByIdReturningCount(@Param("id") UUID id);
}
