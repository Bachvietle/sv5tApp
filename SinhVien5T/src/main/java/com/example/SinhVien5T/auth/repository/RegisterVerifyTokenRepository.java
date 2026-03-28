package com.example.SinhVien5T.auth.repository;

import com.example.SinhVien5T.user.entity.User;
import com.example.SinhVien5T.auth.entity.RegisterVerifyToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegisterVerifyTokenRepository extends JpaRepository<RegisterVerifyToken, String> {

    public Optional<RegisterVerifyToken> findByToken(String token);

    public void deleteByUser(User user);
}


