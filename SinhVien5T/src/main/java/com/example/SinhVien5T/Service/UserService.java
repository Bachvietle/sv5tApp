package com.example.SinhVien5T.Service;

import com.example.SinhVien5T.Dto.Request.UpdateProfileRequest;
import com.example.SinhVien5T.Dto.Request.UserRegisterRequest;
import com.example.SinhVien5T.Dto.Response.ProfileResponse;
import com.example.SinhVien5T.Entity.User;
import com.example.SinhVien5T.Exception.InvalidFileException;
import com.example.SinhVien5T.Exception.ProfileUpdateException;
import com.example.SinhVien5T.Repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Data
public class UserService {
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    // Hàm Helper

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));
    }

    private String getCourseYear(User user) {
        if (user.getCourseYear() == null) return null;
        int studentYear = Integer.parseInt(user.getCourseYear());
        int courseYear = Year.now().getValue() - studentYear;
        return courseYear + "";
    }

    private ProfileResponse buildProfileResponse(User user) {
        return ProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .avatar(user.getAvatar())
                .DoB(user.getDoB())
                .gender(user.getGender())
                .ethnicity(user.getEthnicity())
                .idenNumber(user.getIdenNumber())
                .university(user.getUniversity())
                .fieldOfStudy(user.getFieldOfStudy())
                .courseYear(getCourseYear(user))
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
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public ProfileResponse getProfile() {
        User user = getCurrentUser();
        return buildProfileResponse(user);
    }

    @Transactional
    public ProfileResponse updateProfile(UpdateProfileRequest request, MultipartFile avatarFile) {
        User user = getCurrentUser();
        try {
            if (request.getFirstName() != null)          user.setFirstName(request.getFirstName());
            if (request.getLastName() != null)           user.setLastName(request.getLastName());
            if (request.getDoB() != null)                user.setDoB(request.getDoB());
            if (request.getGender() != null)             user.setGender(request.getGender());
            if (request.getEthnicity() != null)          user.setEthnicity(request.getEthnicity());
            if (request.getUniversity() != null)         user.setUniversity(request.getUniversity());
            if (request.getFieldOfStudy() != null)       user.setFieldOfStudy(request.getFieldOfStudy());
            if (request.getIdenNumber() != null)         user.setIdenNumber(request.getIdenNumber());
            if (request.getProvince() != null)           user.setProvince(request.getProvince());
            if (request.getCommune() != null)            user.setCommune(request.getCommune());
            if (request.getSpecificAddress() != null)    user.setSpecificAddress(request.getSpecificAddress());
            if (request.getProvinceTemp() != null)       user.setProvinceTemp(request.getProvinceTemp());
            if (request.getCommuneTemp() != null)        user.setCommuneTemp(request.getCommuneTemp());
            if (request.getSpecificAddressTemp() != null) user.setSpecificAddressTemp(request.getSpecificAddressTemp());
            if (request.getStudentCode() != null)        user.setStudentCode(request.getStudentCode());
            if (request.getFaculty() != null)            user.setFaculty(request.getFaculty());
            if (request.getCourseYear() != null)         user.setCourseYear(request.getCourseYear());
            if (request.getClassCode() != null)          user.setClassCode(request.getClassCode());
            if (request.getCurrentPosition() != null)    user.setCurrentPosition(request.getCurrentPosition());
            if (request.getPhoneNumber() != null)        user.setPhoneNumber(request.getPhoneNumber());
            if (request.getOrganPosition() != null)      user.setOrganPosition(request.getOrganPosition());
            if (request.getYdMember() != null)           user.setYdMember(request.getYdMember());


            if (avatarFile != null && !avatarFile.isEmpty()) {
                fileStorageService.deleteAvatar(user.getAvatar());
                String filepath = fileStorageService.saveAvatar(avatarFile);
                user.setAvatar(filepath);
            }

            if (!user.isProfileCompleted()) {
                user.setProfileCompleted(true);
            }

            User savedUser = userRepository.save(user);
            return buildProfileResponse(savedUser);

        } catch (InvalidFileException e) {
            throw e;
        } catch (Exception e) {
            throw new ProfileUpdateException("Cập nhật thất bại: " + e.getMessage());
        }

    }
}
