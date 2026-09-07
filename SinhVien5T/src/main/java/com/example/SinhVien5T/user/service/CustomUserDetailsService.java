package com.example.SinhVien5T.user.service;

import com.example.SinhVien5T.user.entity.CustomUserDetails;
import com.example.SinhVien5T.user.entity.User;
import com.example.SinhVien5T.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getUserPassword(),
                user.isVerified(),
                user.isActive(),
                List.of(new SimpleGrantedAuthority("role:" + user.getRole().name()))
        );
    }
}
