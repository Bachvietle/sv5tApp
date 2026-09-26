package com.example.SinhVien5T.user.service;

import com.example.SinhVien5T.common.exception.ConflictException;
import com.example.SinhVien5T.common.exception.ResourceNotFoundException;
import com.example.SinhVien5T.user.dto.request.StudentFilterRequest;
import com.example.SinhVien5T.user.dto.request.UpdateStudentStatusRequest;
import com.example.SinhVien5T.user.dto.response.PageResponse;
import com.example.SinhVien5T.user.dto.response.StudentDetailResponse;
import com.example.SinhVien5T.user.dto.response.StudentSummaryResponse;
import com.example.SinhVien5T.user.entity.Role;
import com.example.SinhVien5T.user.entity.User;
import com.example.SinhVien5T.user.mapper.UserMapper;
import com.example.SinhVien5T.user.repository.UserRepository;
import com.example.SinhVien5T.user.repository.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentManagementService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public PageResponse<StudentSummaryResponse> getStudents(StudentFilterRequest request) {
        if (request == null) {
            request = new StudentFilterRequest();
        }

        // Cấu hình sắp xếp: Mặc định theo bảng chữ cái A-Z (firstName ASC, lastName ASC)
        Sort sort;
        if (StringUtils.hasText(request.getSortBy())) {
            Sort.Direction direction = "DESC".equalsIgnoreCase(request.getSortDirection())
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            sort = Sort.by(direction, request.getSortBy().trim());
        } else {
            sort = Sort.by(Sort.Order.asc("firstName"), Sort.Order.asc("lastName"));
        }

        int page = (request.getPage() != null && request.getPage() >= 0) ? request.getPage() : 0;
        int size = (request.getSize() != null && request.getSize() > 0) ? request.getSize() : 10;

        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<User> spec = UserSpecification.getStudentsFilter(request);

        Page<User> userPage = userRepository.findAll(spec, pageable);

        List<StudentSummaryResponse> content = userPage.getContent().stream()
                .map(userMapper::toStudentSummaryResponse)
                .toList();

        return PageResponse.<StudentSummaryResponse>builder()
                .content(content)
                .pageNumber(userPage.getNumber())
                .pageSize(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .isLast(userPage.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public StudentDetailResponse getStudentDetail(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sinh viên với ID: " + id));

        if (user.getRole() != Role.USER) {
            throw new ResourceNotFoundException("Người dùng không phải là sinh viên");
        }

        return userMapper.toStudentDetailResponse(user);
    }

    @Transactional
    public void updateStudentStatus(Long id, UpdateStudentStatusRequest request) {
        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sinh viên với ID: " + id));

        // Không cho phép tác động lên tài khoản ADMIN
        if (targetUser.getRole() == Role.ADMIN) {
            throw new ConflictException("Không thể thay đổi trạng thái của tài khoản Quản trị viên");
        }

        targetUser.setActive(request.getIsActive());
        userRepository.save(targetUser);
    }
}
