package com.example.SinhVien5T.user.repository;

import com.example.SinhVien5T.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    public boolean existsByEmail(String email);

    public Optional<User> findByEmail(String email);
}


