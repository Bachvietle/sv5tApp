# SinhVien5T — Progress & Context Log

## 1. Feature: Student Management (Admin & Mentor) — [COMPLETED]
* **Mục tiêu:** Quản lý tài khoản sinh viên (xem danh sách, xem chi tiết, khóa/mở khóa).
* **Quyền hạn:** Cả `role:ADMIN` và `role:MENTOR` đều có quyền thao tác. Không ai được phép sửa thông tin cá nhân của sinh viên hay khóa tài khoản ADMIN.

### APIs Đã Triển Khai
* `GET /management/students`: Lấy danh sách sinh viên có phân trang và lọc.
  * **Default Sort:** Bảng chữ cái A-Z (`firstName ASC, lastName ASC`).
  * **Filters:** `keyword` (tìm theo MSSV, Email, Họ tên), `faculty`, `courseYear`, `isActive`, `isProfileCompleted`.
* `GET /management/students/{id}`: Xem chi tiết toàn bộ lý lịch sinh viên (không lộ mật khẩu).
* `PATCH /management/students/{id}/status`: Khóa / Mở khóa tài khoản (`isActive`). Chặn không cho khóa ADMIN.

### Các File Đã Tạo / Cập Nhật
* **DTO:** `StudentFilterRequest`, `UpdateStudentStatusRequest`, `StudentSummaryResponse`, `StudentDetailResponse`, `PageResponse<T>` (`user/dto/`).
* **Specification:** `UserSpecification` (ép `role = Role.USER`, dynamic WHERE query).
* **Repository:** `UserRepository` (kế thừa `JpaSpecificationExecutor<User>`).
* **Mapper:** `UserMapper` (MapStruct ghép `fullName`, map `profileCompleted`).
* **Service:** `StudentManagementService`.
* **Controller:** `StudentManagementController`.
* **Security:** `SecurityConfig` (phân quyền `.hasAnyAuthority("role:ADMIN", "role:MENTOR")`).

---

## 2. Dự kiến tiếp theo — [PLANNED]
* Sẽ được quyết định và triển khai theo yêu cầu tiếp theo (ví dụ: Application Management, Quản trị Mentor, hoặc các module nghiệp vụ khác).
