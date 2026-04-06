package com.example.SinhVien5T.user.service;

import com.example.SinhVien5T.user.dto.request.UpdateProfileRequest;
import com.example.SinhVien5T.user.entity.CustomUserDetails;
import com.example.SinhVien5T.user.entity.User;
import com.example.SinhVien5T.user.exception.ProfileUpdateException;
import com.example.SinhVien5T.user.exception.UserNotFoundException;
import com.example.SinhVien5T.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Data
public class UserService {
    private final UserRepository userRepository;

    private String calculateCourseYear(String courseYear) {
        if (courseYear == null) return null;
        int studentYear = Integer.parseInt(courseYear);
        return (Year.now().getValue() - studentYear) + "";
    }

    private User getUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
        return userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public User buildUserResponse(User user) {
        return User.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .avatar(user.getAvatar())
                .dob(user.getDob())
                .gender(user.getGender())
                .ethnicity(user.getEthnicity())
                .idenNumber(user.getIdenNumber())
                .university(user.getUniversity())
                .fieldOfStudy(user.getFieldOfStudy())
                .courseYear(calculateCourseYear(user.getCourseYear()))
                .studentCode(user.getStudentCode())
                .classCode(user.getClassCode())
                .faculty(user.getFaculty())
                .currentPosition(user.getCurrentPosition())
                .province(user.getProvince())
                .commune(user.getCommune())
                .specificAddress(user.getSpecificAddress())
                .provinceTemp(user.getProvinceTemp())
                .communeTemp(user.getCommuneTemp())
                .specificAddressTemp(user.getSpecificAddressTemp())
                .phoneNumber(user.getPhoneNumber())
                .organPosition(user.getOrganPosition())
                .ydMember(user.getYdMember())
                .profileCompleted(user.isProfileCompleted())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();

    }

    public User getProfile() {
        User userDetails = getUserDetails();
        return buildUserResponse(userDetails);
    }

    @Transactional
    public User updateProfile(UpdateProfileRequest request) {
        User user = getUserDetails();

        try {
            if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
            if (request.getLastName() != null) user.setLastName(request.getLastName());
            if (request.getDob() != null) user.setDob(request.getDob());
            if (request.getGender() != null) user.setGender(request.getGender());
            if (request.getEthnicity() != null) user.setEthnicity(request.getEthnicity());
            if (request.getIdenNumber() != null) user.setIdenNumber(request.getIdenNumber());
            if (request.getUniversity() != null) user.setUniversity(request.getUniversity());
            if (request.getFieldOfStudy() != null) user.setFieldOfStudy(request.getFieldOfStudy());
            if (request.getCourseYear() != null) user.setCourseYear(request.getCourseYear());
            if (request.getStudentCode() != null) user.setStudentCode(request.getStudentCode());
            if (request.getClassCode() != null) user.setClassCode(request.getClassCode());
            if (request.getFaculty() != null) user.setFaculty(request.getFaculty());
            if (request.getCurrentPosition() != null) user.setCurrentPosition(request.getCurrentPosition());
            if (request.getProvince() != null) user.setProvince(request.getProvince());
            if (request.getCommune() != null) user.setCommune(request.getCommune());
            if (request.getSpecificAddress() != null) user.setSpecificAddress(request.getSpecificAddress());
            if (request.getProvinceTemp() != null) user.setProvinceTemp(request.getProvinceTemp());
            if (request.getCommuneTemp() != null) user.setCommuneTemp(request.getCommuneTemp());
            if (request.getSpecificAddressTemp() != null) user.setSpecificAddressTemp(request.getSpecificAddressTemp());
            if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
            if (request.getOrganPosition() != null) user.setOrganPosition(request.getOrganPosition());
            if (request.getYdMember() != null) user.setYdMember(request.getYdMember());


            if (!user.isProfileCompleted()) {
                user.setProfileCompleted(true);
            }

            User savedUser = userRepository.save(user);
            return buildUserResponse(savedUser);
        } catch (Exception e) {
            throw new ProfileUpdateException("Cập nhật thất bại: " + e.getMessage());
        }
    }
}



