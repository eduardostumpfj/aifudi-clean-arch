package com.aifudi_clean_arch.backend.core.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class Kitchen {
    private UUID id;
    private String value;
    private String label;
}
