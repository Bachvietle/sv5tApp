package com.example.SinhVien5T.user.service;

import com.example.SinhVien5T.user.dto.request.UpdateProfileRequest;
import com.example.SinhVien5T.user.dto.response.UserProfileResponse;
import com.example.SinhVien5T.user.entity.CustomUserDetails;
import com.example.SinhVien5T.user.entity.User;
import com.example.SinhVien5T.user.exception.ProfileUpdateException;
import com.example.SinhVien5T.user.exception.UserNotFoundException;
import com.example.SinhVien5T.user.mapper.UserMapper;
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
    private final UserMapper userMapper;

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public UserProfileResponse getProfile(Long userId) {
        return userMapper.toUserProfileResponse(getUserById(userId));
    }

    @Transactional
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        User user = getUserById(userId);

        try {
            userMapper.updateProfileFromRequest(request, user);

            if (!user.isProfileCompleted()) {
                user.setProfileCompleted(true);
            }

            userRepository.save(user);
        } catch (Exception e) {
            throw new ProfileUpdateException("Cập nhật thất bại: " + e.getMessage());
        }
    }
}