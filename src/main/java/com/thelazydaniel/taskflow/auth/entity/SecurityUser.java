package com.thelazydaniel.taskflow.auth.entity;


import com.thelazydaniel.taskflow.user.entity.User;
import com.thelazydaniel.taskflow.user.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

public class SecurityUser implements UserDetails {

    private final User user;
    private final Collection<GrantedAuthority> authorities;

    public SecurityUser(User user) {
        this.user = user;
        this.authorities = convertRoleToAuthority(user.getRole());
    }

    private Collection<GrantedAuthority> convertRoleToAuthority(UserRole role) {
        return Set.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    };

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public User getUser() {
        return user;
    }

    @Override
    public String getPassword(){
        return user.getPasswordHash();
    }

    @Override
    public String getUsername(){
        return user.getUsername();
    }
    @Override
    public boolean isAccountNonExpired(){
        return true;
    }

    @Override
    public boolean isAccountNonLocked(){
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired(){
        return true;
    }

    @Override
    public boolean isEnabled(){
        return user.isEnabled();
    }
}
