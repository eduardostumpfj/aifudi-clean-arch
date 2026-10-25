package com.aifudi_clean_arch.backend.infra.database.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name="menu_items")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MenuItemEntity {
    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name="restaurant_id",  nullable = false)
    private RestaurantEntity restaurant;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private String picture;

    @Column(nullable = false)
    private boolean has_delivery;
}
