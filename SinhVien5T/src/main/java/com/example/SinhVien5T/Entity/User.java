package com.example.SinhVien5T.Entity;

import com.example.SinhVien5T.Entity.Enum.Gender;
import com.example.SinhVien5T.Entity.Enum.Role;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_name", nullable = false, unique = true, length = 100)
    private String userName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String userPassword;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "role")
    @Builder.Default
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id")
    private Faculty faculty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private Classes clazz;

    @Column(name = "student_code", length = 50)
    private String studentCode;

    @Column(name = "is_active")
    private boolean isVerified;

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
}
