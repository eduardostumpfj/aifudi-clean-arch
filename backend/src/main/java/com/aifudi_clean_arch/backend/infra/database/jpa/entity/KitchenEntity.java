package com.aifudi_clean_arch.backend.infra.database.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "kitchens")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KitchenEntity {
    @Id
    private UUID id;

    @Column(nullable = false, length = 50, unique = true)
    private String value;

    @Column(nullable = false, length = 50)
    private String label;


}
