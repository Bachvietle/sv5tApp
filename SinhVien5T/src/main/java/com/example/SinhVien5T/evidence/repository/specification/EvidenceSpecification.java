package com.example.SinhVien5T.evidence.repository.specification;

import com.example.SinhVien5T.campaign.entity.Criteria;
import com.example.SinhVien5T.campaign.entity.Standard;
import com.example.SinhVien5T.evidence.dto.request.EvidenceFilterRequest;
import com.example.SinhVien5T.evidence.entity.Evidence;
import com.example.SinhVien5T.user.entity.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class EvidenceSpecification {

    public static Specification<Evidence> filterEvidences(EvidenceFilterRequest request) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (Long.class != query.getResultType()) {
                root.fetch("user", JoinType.INNER);
                root.fetch("criteria", JoinType.INNER).fetch("standard", JoinType.LEFT);
            }

            Join<Evidence, User> userJoin = (Join<Evidence, User>) root.<Evidence, User>join("user", JoinType.INNER);
            Join<Evidence, Criteria> criteriaJoin = (Join<Evidence, Criteria>) root.<Evidence, Criteria>join("criteria", JoinType.INNER);
            Join<Criteria, Standard> standardJoin = criteriaJoin.join("standard", JoinType.LEFT);

            if (request.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), request.getStatus()));
            }

            if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
                String likeKeyword = "%" + request.getKeyword().toLowerCase() + "%";
                Predicate firstNamePredicate = criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("firstName")), likeKeyword);
                Predicate lastNamePredicate = criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("lastName")), likeKeyword);
                Predicate mssvPredicate = criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("studentCode")), likeKeyword);
                predicates.add(criteriaBuilder.or(firstNamePredicate, lastNamePredicate, mssvPredicate));
            }

            if (request.getCourseYear() != null && !request.getCourseYear().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(userJoin.get("courseYear"), request.getCourseYear()));
            }

            if (request.getStandardId() != null) {
                predicates.add(criteriaBuilder.equal(standardJoin.get("id"), request.getStandardId()));
            }

            if (request.getIsResubmitted() != null) {
                if (request.getIsResubmitted()) {
                    predicates.add(criteriaBuilder.greaterThan(root.get("submissionCount"), 1));
                } else {
                    predicates.add(criteriaBuilder.equal(root.get("submissionCount"), 1));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
