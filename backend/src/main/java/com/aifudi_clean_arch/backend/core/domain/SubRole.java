package com.aifudi_clean_arch.backend.core.domain;

import lombok.Getter;

import java.util.UUID;

@Getter
public class SubRole {
    private final UUID id;
    private final String value;
    private final String label;
    private final Role role;

    public SubRole(UUID id, String value, String label, Role role){
        if(value == null || value.isBlank()){
            throw new IllegalArgumentException("Value não pode ser nulo.");
        }
        if(label == null || label.isBlank() ){
            throw new IllegalArgumentException("Label não pode ser nulo.");
        }
        if(role == null){
            throw new IllegalArgumentException("Role não pode ser nulo.");
        }

        this.id = id;
        this.value = value;
        this.label = label;
        this.role = role;
    }

    public boolean isMainOwner(){
        return "main".equals(value) &&  "OWNER".equals(role.getValue());
    }
}
