package com.example.SinhVien5T.campaign.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;


import java.text.NumberFormat;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

@Entity
@Table(name = "standard")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Standard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    @JsonIgnore // tránh vòng lặp vô tận khi map Object -> JSON
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Campaign campaign;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "standard")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @SQLRestriction("parent_id IS NULL")
    private Set<Criteria> criteriaList = new LinkedHashSet<>();

}


