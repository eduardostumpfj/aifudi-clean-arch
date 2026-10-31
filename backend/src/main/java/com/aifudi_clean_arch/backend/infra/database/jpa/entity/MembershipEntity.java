package com.aifudi_clean_arch.backend.infra.database.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;
import org.hibernate.annotations.Generated;

@Entity
@Table(
        name="memberships",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_membership_user_restaurant",
                columnNames = {"user_id", "restaurant_id"}
        )
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MembershipEntity {
    @Id
    @Generated
    private UUID id;

    @ManyToOne
    @JoinColumn(name="user_id", nullable=false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name="subrole_id", nullable=false)
    private SubRoleEntity subrole;

    @ManyToOne
    @JoinColumn(name="restaurant_id", nullable=false)
    private RestaurantEntity restaurant;
}
