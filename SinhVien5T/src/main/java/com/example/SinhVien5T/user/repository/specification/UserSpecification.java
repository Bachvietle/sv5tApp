package com.example.SinhVien5T.user.repository.specification;

import com.example.SinhVien5T.user.dto.request.StudentFilterRequest;
import com.example.SinhVien5T.user.entity.Role;
import com.example.SinhVien5T.user.entity.User;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<User> getStudentsFilter(StudentFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Luôn luôn chỉ lấy tài khoản sinh viên (Role.USER)
            predicates.add(cb.equal(root.get("role"), Role.USER));

            if (filter == null) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            // 2. Tìm kiếm theo keyword (MSSV, Email, Họ và tên)
            if (StringUtils.hasText(filter.getKeyword())) {
                String keywordPattern = "%" + filter.getKeyword().trim().toLowerCase() + "%";

                Expression<String> fullName = cb.concat(
                        cb.concat(
                                cb.coalesce(root.get("lastName"), ""),
                                " "
                        ),
                        cb.coalesce(root.get("firstName"), "")
                );

                Predicate studentCodeMatch = cb.like(cb.lower(cb.coalesce(root.get("studentCode"), "")), keywordPattern);
                Predicate emailMatch = cb.like(cb.lower(root.get("email")), keywordPattern);
                Predicate nameMatch = cb.like(cb.lower(fullName), keywordPattern);

                predicates.add(cb.or(studentCodeMatch, emailMatch, nameMatch));
            }

            // 3. Lọc theo Khoa (Faculty)
            if (StringUtils.hasText(filter.getFaculty())) {
                predicates.add(cb.equal(cb.lower(root.get("faculty")), filter.getFaculty().trim().toLowerCase()));
            }

            // 4. Lọc theo Khóa học (CourseYear, ví dụ: K20, 2021)
            if (StringUtils.hasText(filter.getCourseYear())) {
                predicates.add(cb.equal(root.get("courseYear"), filter.getCourseYear().trim()));
            }

            // 5. Lọc theo trạng thái hoạt động / khóa (isActive)
            if (filter.getIsActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), filter.getIsActive()));
            }

            // 6. Lọc theo trạng thái hoàn thành hồ sơ (isProfileCompleted)
            if (filter.getIsProfileCompleted() != null) {
                predicates.add(cb.equal(root.get("profileCompleted"), filter.getIsProfileCompleted()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
