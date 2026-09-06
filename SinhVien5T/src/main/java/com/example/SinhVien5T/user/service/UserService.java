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
    public void updateProfile(UpdateProfileRequest request) {
        User user = getUserDetails();

        try {



            if (!user.isProfileCompleted()) {
                user.setProfileCompleted(true);
            }

            userRepository.save(user);
        } catch (Exception e) {
            throw new ProfileUpdateException("Cập nhật thất bại: " + e.getMessage());
        }
    }
}



