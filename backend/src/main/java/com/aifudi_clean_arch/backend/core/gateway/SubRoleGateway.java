package com.aifudi_clean_arch.backend.core.gateway;

import com.aifudi_clean_arch.backend.core.domain.SubRole;

import java.util.Optional;
import java.util.UUID;


public interface SubRoleGateway {
    void save(SubRole subRole);
    Optional <SubRole> getById(UUID id);
    Optional <SubRole> getByValue(String value);
    boolean delete(UUID subrole_id);
    SubRole update(SubRole subRole);
}
