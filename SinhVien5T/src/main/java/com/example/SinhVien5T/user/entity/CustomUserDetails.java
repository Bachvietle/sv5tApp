package com.example.SinhVien5T.user.entity;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class CustomUserDetails extends User {

    private final Long id;

    // Constructor 1: Dùng cho JwtAuthenticationFilter (dựng Proxy User)
    // Giả định token còn hạn thì mọi cờ đều là true
    public CustomUserDetails(Long id, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password != null ? password : "", authorities);
        this.id = id;
    }

    // Constructor 2: Dùng cho CustomUserDetailsService (Lúc query DB đăng nhập)
    // Truyền đầy đủ trạng thái khóa tài khoản, xác thực email
    public CustomUserDetails(Long id, String username, String password,
                             boolean enabled, boolean accountNonLocked,
                             Collection<? extends GrantedAuthority> authorities) {
        super(
                username,
                password != null ? password : "",
                enabled, // Map với isVerified
                true,    // accountNonExpired (Mặc định true)
                true,    // credentialsNonExpired (Mặc định true)
                accountNonLocked, // Map với isActive
                authorities
        );
        this.id = id;
    }
}
