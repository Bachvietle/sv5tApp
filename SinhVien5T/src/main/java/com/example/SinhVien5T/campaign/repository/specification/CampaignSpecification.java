package com.example.SinhVien5T.campaign.repository.specification;

import com.example.SinhVien5T.campaign.dto.CampaignFilterRequest;
import com.example.SinhVien5T.campaign.entity.Campaign;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CampaignSpecification {

    public static Specification<Campaign> filterCampaigns(CampaignFilterRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request == null) {
                return criteriaBuilder.conjunction();
            }

            if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
                String likeKeyword = "%" + request.getKeyword().trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likeKeyword));
            }

            if (request.getAcademicYear() != null) {
                predicates.add(criteriaBuilder.equal(root.get("academicYear"), request.getAcademicYear()));
            }

            if (request.getLevel() != null) {
                predicates.add(criteriaBuilder.equal(root.get("level"), request.getLevel()));
            }

            if (request.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), request.getStatus()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
