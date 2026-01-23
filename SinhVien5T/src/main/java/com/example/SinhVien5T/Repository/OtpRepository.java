package com.example.SinhVien5T.Repository;

import com.example.SinhVien5T.Entity.VerifyToken.Otp;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface OtpRepository  extends JpaRepository<Otp, String> {
    Optional<Otp> findByEmail(String token);
    void deleteByEmail(String email);
}
