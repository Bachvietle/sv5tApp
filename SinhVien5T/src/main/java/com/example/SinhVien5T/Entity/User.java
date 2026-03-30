package com.example.SinhVien5T.Entity;

import com.example.SinhVien5T.Entity.Enum.Gender;
import com.example.SinhVien5T.Entity.Enum.Role;
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
import org.springframework.security.core.userdetails.UserDetailsService;

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

    @Column(name = "user_name", nullable = false, unique = true, length = 100)
    private String userName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String userPassword;

    @Column(name = "role")
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @Column (name = "is_profile_completed")
    @Builder.Default
    private boolean isProfileCompleted = false;

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

    @Column (name = "f_name", length = 50)
    private String firstName;

    @Column (name = "l_name", length = 100)
    private String lastName;

    private String avatar;

    @Column (name = "birth_day")
    private LocalDate DoB;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column (name = "ethnicity", length = 20)
    private String ethnicity;

    @Column (name = "iden_number", length = 20)
    private String idenNumber;

    @Column (name = "university", length = 20)
    @Builder.Default
    private String university = "Đại học Hà Nội";

    @Column (name = "field_of_study", length = 50)
    private String fieldOfStudy;

    @Column (name = "course_year", length = 4)
    private String courseYear;

    @Column (name = "student_code", length = 10)
    private String studentCode;

    @Column (name = "class_code", length = 20)
    private String classCode;

    @Column (name = "faculty", length = 50)
    private String faculty;

    @Column (name = "current_position", length = 20)
    private String currentPosition;

    @Column (name = "province")
    private String province;

    @Column (name = "commune")
    private String commune;

    @Column (name = "specific_address", columnDefinition = "TEXT")
    private String specificAddress;

    @Column (name = "province_temp")
    private String provinceTemp;

    @Column (name = "commune_temp")
    private String communeTemp;

    @Column (name = "specific_address_temp", columnDefinition = "TEXT")
    private String specificAddressTemp;

    @Column (name = "phone_number", length = 10)
    private String phoneNumber;

    @Column (name = "organ_position", length = 50)
    @Builder.Default
    private String organPosition = "Không";

    @Column (name = "yd_member")
    private String ydMember;

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
