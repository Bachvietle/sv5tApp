package com.example.SinhVien5T.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_login_reset_otps")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Otp {
    @Id
    private String id;

    @Column(nullable = false, length = 128)
    private String email;

    @Column(name = "otp_hash", nullable = false, length = 255)
    private String otp;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;
}


