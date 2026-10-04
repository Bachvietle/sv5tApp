# SinhVien5T - Progress & Context Log

## 1. Feature: Student Management (Admin & Mentor) - [COMPLETED]
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
* **Specification:** `UserSpecification` (Ép `role = Role.USER`, dynamic WHERE query).
* **Repository:** `UserRepository` (kế thừa `JpaSpecificationExecutor<User>`).
* **Mapper:** `UserMapper` (MapStruct ghép `fullName`, map `profileCompleted`).
* **Service:** `StudentManagementService`.
* **Controller:** `StudentManagementController`.
* **Security:** `SecurityConfig` (phân quyền `.hasAnyAuthority("role:ADMIN", "role:MENTOR")`).

---

## 2. Feature: Evidence Management (Admin & Mentor) - [COMPLETED]
* **Mục tiêu:** Quản lý và xét duyệt các minh chứng (Evidence) sinh viên tải lên cho từng tiêu chí.
* **Quyền hạn:** Cấp `role:ADMIN` và `role:MENTOR`.

### APIs Đã Triển Khai
* `GET /management/evidences`: Lấy danh sách minh chứng có phân trang và lọc.
  * **Default Sort:** Lọc theo thời gian nộp (`submittedAt DESC`).
  * **Filters:** `keyword` (tìm theo MSSV, Họ tên sinh viên), `campaignId`, `standardId`, `criteriaId`, `status`, `isResubmitted` (những minh chứng bị nộp lại `submissionCount > 1`).
* `PATCH /management/evidences/{id}/review`: Cán bộ xét duyệt đánh giá minh chứng.
  * **Payload:** `status` (APPROVED, REJECTED, NEEDS_MODIFICATION), `reviewerComment`.
  * **Logic:** Cập nhật trạng thái, lưu `reviewer_id` và `reviewed_at`.

### Các File Đã Tạo / Cập Nhật
* **Cấu trúc DTO & Entity:**
  * Thêm `EvidenceStatus` (Enum).
  * Chuyển `PageResponse` sang thư mục chung `common/dto/response`.
  * Chuyển `EvidenceRepository` sang `evidence/repository`.
  * Tạo các class DTO: `EvidenceFilterRequest`, `EvidenceReviewRequest`, `EvidenceAdminDTO`.
* **Specification & Mapper:** 
  * `EvidenceSpecification` (Fetch lazy properties, filter theo keyword FirstName, LastName, StudentCode).
  * `EvidenceMapper` (Dùng MapStruct, có default method buildFullName).
* **Service:** `EvidenceManagementService` (Chứa logic xử lý nghiệp vụ xét duyệt).
* **Controller:** `EvidenceManagementController` (Sử dụng `@ModelAttribute` cho filter gọn gàng).
* Tích hợp: Fix lại các lỗi logic (NPE, Type Mismatch) xuất hiện trong `CampaignService` và `EvidenceService`.

---

## 3. Dự kiến tiếp theo - [PLANNED]
* **Feature: Quản lý Hồ sơ xét tuyển (Application)**.
* Tạo thực thể `Application` đại diện cho một hồ sơ hoàn chỉnh.
* Xây dựng API Submit cho sinh viên và API quản lý Application cho Admin.
