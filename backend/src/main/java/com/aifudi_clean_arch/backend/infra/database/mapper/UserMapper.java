package com.aifudi_clean_arch.backend.infra.database.mapper;

import com.aifudi_clean_arch.backend.core.domain.SubRole;
import com.aifudi_clean_arch.backend.core.domain.User;
import com.aifudi_clean_arch.backend.infra.database.jpa.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {
    private final SubRoleMapper subRoleMapper;

    public User map(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getLogin(),
                entity.getHashed_password(),
                entity.getAddress(),
                subRoleMapper.map(entity.getSubRole())
        );
    }

    public UserEntity map(User user){
        return new UserEntity(
                user.getId(),
                subRoleMapper.map(user.getClientSubrole()),
                user.getName(),
                user.getLogin(),
                user.getEmail(),
                user.getHashedPassword(),
                user.getAddress()
        );
    }
}
