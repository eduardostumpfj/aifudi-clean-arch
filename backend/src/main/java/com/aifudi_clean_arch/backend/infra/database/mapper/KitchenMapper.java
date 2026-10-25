package com.aifudi_clean_arch.backend.infra.database.mapper;

import com.aifudi_clean_arch.backend.core.domain.Kitchen;
import com.aifudi_clean_arch.backend.infra.database.jpa.entity.KitchenEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KitchenMapper {
    public Kitchen map(KitchenEntity entity) {
        return new  Kitchen(entity.getId(), entity.getValue(), entity.getLabel());
    }

    public KitchenEntity map(Kitchen entity) {
        return new  KitchenEntity(entity.getId(), entity.getValue(), entity.getLabel());
    }

}
