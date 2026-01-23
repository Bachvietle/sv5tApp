package com.example.SinhVien5T.Repository;

import com.example.SinhVien5T.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    public boolean existsByEmail(String email);

    public Optional<User> findByEmail(String email);
}
