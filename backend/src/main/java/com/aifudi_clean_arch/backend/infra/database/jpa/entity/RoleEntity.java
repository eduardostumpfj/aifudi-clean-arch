package com.aifudi_clean_arch.backend.infra.database.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;
import org.hibernate.annotations.Generated;

@Entity
@Table(name = "roles")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleEntity {
    @Id
    @Generated
    private UUID id;

    @Column(nullable = false, length = 50, unique = true)
    private String value;

    @Column(nullable = false, length = 50)
    private String label;
}
