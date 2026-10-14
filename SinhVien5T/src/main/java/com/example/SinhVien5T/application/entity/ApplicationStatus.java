package com.example.SinhVien5T.application.entity;

public enum ApplicationStatus {
    SUBMITTED,   // Đã nộp hồ sơ, chờ Hội đồng thẩm định
    APPROVED,    // Đạt danh hiệu Sinh viên 5 Tốt
    REJECTED,    // Không đạt danh hiệu
    CANCELLED    // Sinh viên đã hủy nộp trước hạn
}
