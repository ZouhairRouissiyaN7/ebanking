package com.ebanking.auth_service.entities;

import com.ebanking.auth_service.enums.Role;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "app_users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean enabled = true;

    private Long customerId;

    public AppUser(
            String username,
            String passwordHash,
            Role role,
            Long customerId) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.customerId = customerId;
    }
}
