package com.aifudi_clean_arch.backend.core.domain;

import lombok.Getter;

import java.util.Set;
import java.util.UUID;

@Getter
public class Restaurant {
    private UUID id;
    private String name;
    private String address;
    private String openingTime;
    private Kitchen kitchen;
    private Set<Membership> members;
    private Set<MenuItem> menuItems;

    public Restaurant(UUID id, String name, String address, String openingTime, Kitchen kitchen, Set<Membership> members, Set<MenuItem> menuItems) {
        if (id == null) {
            throw new IllegalArgumentException("id é obrigatório.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name é obrigatório.");
        }
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("address é obrigatório.");
        }
        if (openingTime == null || openingTime.isBlank()) {
            throw new IllegalArgumentException("openingTime é obrigatório.");
        }
        if (kitchen == null) {
            throw new IllegalArgumentException("kitchen é obrigatório.");
        }
        if (members == null) {
            throw new IllegalArgumentException("members é obrigatório.");
        }
        if (menuItems == null) {
            throw new IllegalArgumentException("menuItems é obrigatório.");
        }
        this.id = id;
        this.name = name;
        this.address = address;
        this.openingTime = openingTime;
        this.kitchen = kitchen;
        this.members = members;
        this.menuItems = menuItems;
    }
}
