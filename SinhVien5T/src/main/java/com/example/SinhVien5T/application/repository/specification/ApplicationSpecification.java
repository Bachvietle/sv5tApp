package com.example.SinhVien5T.application.repository.specification;

import com.example.SinhVien5T.application.dto.request.ApplicationFilterRequest;
import com.example.SinhVien5T.application.entity.Application;
import com.example.SinhVien5T.campaign.entity.Campaign;
import com.example.SinhVien5T.user.entity.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ApplicationSpecification {

    public static Specification<Application> filterApplications(ApplicationFilterRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request == null) {
                return criteriaBuilder.conjunction();
            }

            if (Long.class != query.getResultType()) {
                root.fetch("student", JoinType.INNER);
                root.fetch("campaign", JoinType.INNER);
            }

            Join<Application, User> studentJoin = (Join<Application, User>) root.<Application, User>join("student", JoinType.INNER);
            Join<Application, Campaign> campaignJoin = (Join<Application, Campaign>) root.<Application, Campaign>join("campaign", JoinType.INNER);

            if (request.getCampaignId() != null) {
                predicates.add(criteriaBuilder.equal(campaignJoin.get("id"), request.getCampaignId()));
            }

            if (request.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), request.getStatus()));
            }

            if (request.getPassedStandardsCount() != null) {
                predicates.add(criteriaBuilder.equal(root.get("passedStandardsCount"), request.getPassedStandardsCount()));
            }

            if (request.getFaculty() != null && !request.getFaculty().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(studentJoin.get("faculty"), request.getFaculty().trim()));
            }

            if (request.getCourseYear() != null && !request.getCourseYear().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(studentJoin.get("courseYear"), request.getCourseYear().trim()));
            }

            if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
                String likeKeyword = "%" + request.getKeyword().trim().toLowerCase() + "%";
                Predicate firstNamePredicate = criteriaBuilder.like(criteriaBuilder.lower(studentJoin.get("firstName")), likeKeyword);
                Predicate lastNamePredicate = criteriaBuilder.like(criteriaBuilder.lower(studentJoin.get("lastName")), likeKeyword);
                Predicate codePredicate = criteriaBuilder.like(criteriaBuilder.lower(studentJoin.get("studentCode")), likeKeyword);
                predicates.add(criteriaBuilder.or(firstNamePredicate, lastNamePredicate, codePredicate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
