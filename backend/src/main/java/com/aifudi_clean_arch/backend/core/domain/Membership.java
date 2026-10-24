package com.aifudi_clean_arch.backend.core.domain;

import lombok.Getter;

import java.util.UUID;

@Getter
public class Membership {
    private UUID id;
    private User user;
    private SubRole subrole;
    private Restaurant restaurant;

    public Membership(UUID id, User user, SubRole subrole, Restaurant restaurant) {
        if (id == null) {
            throw new IllegalArgumentException("id é obrigatório.");
        }
        if (user == null) {
            throw new IllegalArgumentException("user é obrigatório.");
        }
        if (subrole == null) {
            throw new IllegalArgumentException("subrole é obrigatório.");
        }
        if (restaurant == null) {
            throw new IllegalArgumentException("restaurant é obrigatório.");
        }
        this.id = id;
        this.user = user;
        this.subrole = subrole;
        this.restaurant = restaurant;
    }
}
