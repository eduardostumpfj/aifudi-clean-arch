package com.aifudi_clean_arch.backend.infra.database.jpa.repository;

import com.aifudi_clean_arch.backend.infra.database.jpa.entity.RestaurantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface RestaurantRepository extends JpaRepository<RestaurantEntity, UUID> {
    @Modifying
    @Query("delete from RestaurantEntity entity where entity.id = :id")
    int deleteByIdReturningCount(@Param("id") UUID id);
}
