package com.example.SinhVien5T.user.repository;

import com.example.SinhVien5T.user.entity.Role;
import com.example.SinhVien5T.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    public boolean existsByEmail(String email);

    public Optional<User> findByEmail(String email);

    @Query("SELECT u.email FROM User u WHERE u.role = :role AND u.isActive = true")
    List<String> findEmailsByRoleAndIsActiveTrue(@Param("role") Role role);
}


