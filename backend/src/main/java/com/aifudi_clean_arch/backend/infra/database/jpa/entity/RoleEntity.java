package com.aifudi_clean_arch.backend.infra.database.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "roles")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleEntity {
    @Id
    private UUID id;

    @Column(nullable = false, length = 50, unique = true)
    private String value;

    @Column(nullable = false, length = 50)
    private String label;
}
