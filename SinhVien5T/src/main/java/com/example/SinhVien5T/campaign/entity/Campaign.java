package com.example.SinhVien5T.campaign.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "campaign")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "academic_year", nullable = false)
    private Long academicYear;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Level level;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate end_date;

    @Column(length = 50)
    private String status;

    @CreationTimestamp
    @Column(name = "create_at", updatable = false)
    private LocalDate create_at;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDate update_up;

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL,orphanRemoval = true)
    @EqualsAndHashCode.Exclude // tránh vòng lặp vô tận khi load Object
    @ToString.Exclude
    private Set<Standard> standards = new LinkedHashSet<>();
}


