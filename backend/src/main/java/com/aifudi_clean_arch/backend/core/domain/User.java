package com.aifudi_clean_arch.backend.core.domain;

import lombok.Getter;

import java.util.UUID;

@Getter
public class User {
    private UUID id;
    private String name;
    private String email;
    private String hashedPassword;
    private String address;
    private SubRole subrole;

    public User(UUID id, String name, String email, String hashedPassword, String address, SubRole subrole) {
        if (id == null) {
            throw new IllegalArgumentException("id é obrigatório.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name é obrigatório.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email é obrigatório.");
        }
        if (hashedPassword == null || hashedPassword.isBlank()) {
            throw new IllegalArgumentException("hashedPassword é obrigatório.");
        }
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("address é obrigatório.");
        }
        if (subrole == null) {
            throw new IllegalArgumentException("subrole é obrigatório.");
        }
        this.id = id;
        this.name = name;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.address = address;
        this.subrole = subrole;
    }
}
