package com.pado.global.auth.userdetails;

import com.pado.domain.user.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
    private final User user;

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }
    @Override public String getPassword() { return user.getPasswordHash(); }
    @Override public String getUsername() { return user.getEmail(); }// 로그인 식별자
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
    public User getUser() { return user; }
}