package com.aifudi_clean_arch.backend.infra.database.jpa.repository;

import com.aifudi_clean_arch.backend.infra.database.jpa.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
}
