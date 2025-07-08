package me.jinheum.datelog.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.RequiredArgsConstructor;
import me.jinheum.datelog.entity.UserAccount;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final UserAccount user;

    public UserAccount getUser() {
        return this.user;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return List.of(() -> "ROLE_USER"); 
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}