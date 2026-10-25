package com.aifudi_clean_arch.backend.infra.database.jpa.repository;

import com.aifudi_clean_arch.backend.infra.database.jpa.entity.KitchenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface KitchenRepository extends JpaRepository<KitchenEntity, UUID> {
}
