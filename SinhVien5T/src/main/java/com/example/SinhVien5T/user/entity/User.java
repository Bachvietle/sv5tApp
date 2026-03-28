package com.example.SinhVien5T.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_name", unique = true, length = 100)
    private String userName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String userPassword;

    @Column(name = "role")
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    private String avatar;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    private String ethnicity;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "birth_day")
    private LocalDate birthDay;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "faculty")
    private String faculty;

    @Column(name = "class")
    private String classId;

    @Column(name = "student_code", length = 50)
    private String studentCode;

    @Column(name = "is_verified")
    @Builder.Default
    private boolean isVerified = true;

    @Column(name = "is_active")
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public String getUserName() {
        return this.userName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("role:" + this.role.name()));
    }

    @Override
    public String getPassword() {
        return this.userPassword;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {  // Mặc định true; customize nếu cần thêm field
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {  // Mặc định true
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() { // Mặc định true
        return true;
    }

    @Override
    public boolean isEnabled() {  // Dựa trên field enabled
        return this.isActive;
    }
}


