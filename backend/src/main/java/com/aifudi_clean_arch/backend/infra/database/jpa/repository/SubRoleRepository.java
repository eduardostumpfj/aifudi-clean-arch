package com.aifudi_clean_arch.backend.infra.database.jpa.repository;

import com.aifudi_clean_arch.backend.infra.database.jpa.entity.SubRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SubRoleRepository extends JpaRepository<SubRoleEntity, UUID> {
    Optional<SubRoleEntity> findByValue(String value);
    @Modifying
    @Query("delete from SubRoleEntity entity where entity.id = :id")
    int deleteByIdReturningCount(@Param("id") UUID id);
}
