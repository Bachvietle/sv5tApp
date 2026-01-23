package com.example.SinhVien5T.Repository;

import com.example.SinhVien5T.Entity.User;
import com.example.SinhVien5T.Entity.VerifyToken.RegisterVerifyToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegisterVerifyTokenRepository extends JpaRepository<RegisterVerifyToken, String> {

    public Optional<RegisterVerifyToken> findByToken(String token);

    public void deleteByUser(User user);
}
