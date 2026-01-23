package com.example.SinhVien5T.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "faculties")
@Data

public class Faculty {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "fa_code", nullable = false, unique = true, length = 20)
        private String faCode;

        @Column(name = "fa_name", nullable = false, length = 150)
        private String faName;

        @Column(columnDefinition = "TEXT")
        private String description;

        @Column(name = "is_active")
        private Boolean isActive = true;

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
