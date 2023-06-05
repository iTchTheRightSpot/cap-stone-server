package com.example.sarabrandserver.client.detial;

import com.example.sarabrandserver.client.entity.Clientz;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public record ClientzDetail(Clientz clientz) implements UserDetails {
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.clientz.getAuthorities();
    }

    @Override
    public String getPassword() {
        return this.clientz.getPassword();
    }

    @Override
    public String getUsername() {
        return this.clientz.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.clientz.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.clientz.isLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.clientz.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return this.clientz.isEnabled();
    }
}
