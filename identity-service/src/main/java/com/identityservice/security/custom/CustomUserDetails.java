package com.identityservice.security.custom;


import com.identityservice.entity.auth.entity.UserCredential;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomUserDetails implements UserDetails {

    // 1. Đổi ID sang String để khớp với UserCredential (Snowflake ID)
    private String id;
    private String email;
    private String password;
    private boolean isVerified; // Dùng isVerified thay cho isActive cho đúng logic Identity
    private Collection<? extends GrantedAuthority> authorities;

    // 2. Constructor nạp dữ liệu từ Entity (Dùng cho MyBatis-Plus)
    // Giả sử ông lấy User kèm Set<String> roles từ Service
    public CustomUserDetails(UserCredential user, java.util.Set<String> roles) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPasswordHash();
        this.isVerified = user.isVerified();
        this.authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role))
                .collect(Collectors.toList());
    }

    @Override public String getUsername() { return email; }
    @Override public String getPassword() { return password; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    // Các trạng thái tài khoản
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; } // Sau này có thể map với logic Lock của Redis
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return isVerified; }
}