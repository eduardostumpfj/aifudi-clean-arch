package com.aifudi_clean_arch.backend.infra.database.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "subroles")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubRoleEntity {
    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "role_id",  nullable = false)
    private RoleEntity role;

    @Column(nullable = false, length = 50, unique = true)
    private String value;

    @Column(nullable = false, length = 50)
    private String label;
}
