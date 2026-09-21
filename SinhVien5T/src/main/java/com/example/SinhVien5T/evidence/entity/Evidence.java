package com.example.SinhVien5T.evidence.entity;

import com.example.SinhVien5T.campaign.entity.Criteria;
import com.example.SinhVien5T.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Table(name = "evidence", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "criteria_id"})
})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Evidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criteria_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Criteria criteria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "evidence_url", nullable = false)
    private String evidenceUrl;

    private Boolean status;

    @Column(name = "reviewer_comment")
    private String reviewerComment;

}
