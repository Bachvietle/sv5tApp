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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentManagementServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private StudentManagementService studentManagementService;

    private User studentUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        studentUser = new User();
        studentUser.setId(1L);
        studentUser.setRole(Role.USER);
        studentUser.setActive(true);

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setRole(Role.ADMIN);
        adminUser.setActive(true);
    }

    @Test
    void getStudents_ShouldReturnPageResponse() {
        // Arrange
        StudentFilterRequest request = new StudentFilterRequest();
        request.setPage(0);
        request.setSize(10);
        
        Page<User> userPage = new PageImpl<>(List.of(studentUser));
        
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);
        when(userMapper.toStudentSummaryResponse(any(User.class))).thenReturn(new StudentSummaryResponse());

        // Act
        PageResponse<StudentSummaryResponse> response = studentManagementService.getStudents(request);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        verify(userRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getStudentDetail_WhenStudentExists_ShouldReturnDetail() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(studentUser));
        when(userMapper.toStudentDetailResponse(studentUser)).thenReturn(new StudentDetailResponse());

        // Act
        StudentDetailResponse response = studentManagementService.getStudentDetail(1L);

        // Assert
        assertNotNull(response);
        verify(userRepository, times(1)).findById(1L);
        verify(userMapper, times(1)).toStudentDetailResponse(studentUser);
    }

    @Test
    void getStudentDetail_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> studentManagementService.getStudentDetail(99L));
        verify(userRepository, times(1)).findById(99L);
    }

    @Test
    void getStudentDetail_WhenUserNotStudent_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> studentManagementService.getStudentDetail(2L));
        verify(userRepository, times(1)).findById(2L);
    }

    @Test
    void updateStudentStatus_WhenStudentExists_ShouldUpdateStatus() {
        // Arrange
        UpdateStudentStatusRequest request = new UpdateStudentStatusRequest();
        request.setIsActive(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(studentUser));

        // Act
        studentManagementService.updateStudentStatus(1L, request);

        // Assert
        assertFalse(studentUser.isActive());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(studentUser);
    }

    @Test
    void updateStudentStatus_WhenUserIsAdmin_ShouldThrowConflictException() {
        // Arrange
        UpdateStudentStatusRequest request = new UpdateStudentStatusRequest();
        request.setIsActive(false);

        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));

        // Act & Assert
        assertThrows(ConflictException.class, () -> studentManagementService.updateStudentStatus(2L, request));
        verify(userRepository, times(1)).findById(2L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateStudentStatus_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        UpdateStudentStatusRequest request = new UpdateStudentStatusRequest();
        request.setIsActive(false);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> studentManagementService.updateStudentStatus(99L, request));
        verify(userRepository, times(1)).findById(99L);
        verify(userRepository, never()).save(any(User.class));
    }
}
