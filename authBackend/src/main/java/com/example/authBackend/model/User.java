package com.example.authBackend.model;

import com.example.authBackend.Enum.Provider;
import com.example.authBackend.Enum.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "user")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID id;

    private String image;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private Boolean enable = true;

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    @NotBlank(message = "Name is required minimum 1 in size")
    @Size(min = 1)
    private String name;

    @NotBlank(message = "Email is required")
    @Column(unique = true)
    @Email
    private String email;

    /*@NotBlank*/
    private String password;

    @Enumerated(EnumType.STRING)
    private Provider provider;

    private String providerId;

    @Enumerated(EnumType.STRING)
    private Role role;


    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (role==null) role = Role.USER;
        if (createdAt == null) createdAt = now;
        if (provider == null) provider = Provider.LOCAL;
        if (enable == null) enable = Boolean.TRUE;
        updatedAt = now;
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.toString()));
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.enable != null && this.enable;
    }
}
