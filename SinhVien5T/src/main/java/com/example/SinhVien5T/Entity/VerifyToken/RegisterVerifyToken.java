package com.example.SinhVien5T.Entity.VerifyToken;

import com.example.SinhVien5T.Entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
public class RegisterVerifyToken {

    @Id
    private String token; // UUID

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "expiry_date")
    LocalDateTime expiryDate;

}
