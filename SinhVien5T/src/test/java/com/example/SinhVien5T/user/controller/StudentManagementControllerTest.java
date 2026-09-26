package com.example.SinhVien5T.user.controller;

import com.example.SinhVien5T.user.dto.request.StudentFilterRequest;
import com.example.SinhVien5T.user.dto.request.UpdateStudentStatusRequest;
import com.example.SinhVien5T.user.dto.response.PageResponse;
import com.example.SinhVien5T.user.dto.response.StudentDetailResponse;
import com.example.SinhVien5T.user.dto.response.StudentSummaryResponse;
import com.example.SinhVien5T.user.service.StudentManagementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StudentManagementControllerTest {

    private MockMvc mockMvc;

    @Mock
    private StudentManagementService studentManagementService;

    @InjectMocks
    private StudentManagementController studentManagementController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(studentManagementController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getStudents_ShouldReturnOk() throws Exception {
        // Arrange
        PageResponse<StudentSummaryResponse> pageResponse = PageResponse.<StudentSummaryResponse>builder()
                .content(List.of(new StudentSummaryResponse()))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .isLast(true)
                .build();
        
        when(studentManagementService.getStudents(any(StudentFilterRequest.class))).thenReturn(pageResponse);

        // Act & Assert
        mockMvc.perform(get("/management/students")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Lấy danh sách sinh viên thành công"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void getStudentDetail_ShouldReturnOk() throws Exception {
        // Arrange
        StudentDetailResponse detailResponse = new StudentDetailResponse();
        detailResponse.setId(1L);
        detailResponse.setEmail("student@example.com");

        when(studentManagementService.getStudentDetail(1L)).thenReturn(detailResponse);

        // Act & Assert
        mockMvc.perform(get("/management/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Lấy thông tin chi tiết sinh viên thành công"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void updateStudentStatus_WhenLocking_ShouldReturnOk() throws Exception {
        // Arrange
        UpdateStudentStatusRequest request = new UpdateStudentStatusRequest(false);
        
        doNothing().when(studentManagementService).updateStudentStatus(eq(1L), any(UpdateStudentStatusRequest.class));

        // Act & Assert
        mockMvc.perform(patch("/management/students/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Khóa tài khoản sinh viên thành công"));
    }

    @Test
    void updateStudentStatus_WhenUnlocking_ShouldReturnOk() throws Exception {
        // Arrange
        UpdateStudentStatusRequest request = new UpdateStudentStatusRequest(true);
        
        doNothing().when(studentManagementService).updateStudentStatus(eq(1L), any(UpdateStudentStatusRequest.class));

        // Act & Assert
        mockMvc.perform(patch("/management/students/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Mở khóa tài khoản sinh viên thành công"));
    }
}
