package com.example.SinhVien5T.Repository;

import com.example.SinhVien5T.Entity.VerifyToken.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    public Optional<RefreshToken> findByToken(String token);
}
