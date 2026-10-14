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

## 3. Feature: Quản lý Đợt xét tuyển (Campaign Lifecycle & Grouping) - [COMPLETED]
* **Mục tiêu:** Quản lý vòng đời đợt xét tuyển (UPCOMING, ACTIVE, CLOSED, PAUSED), gom nhóm đợt theo năm học và sắp xếp thông minh.
* **Cơ chế Hybrid & Tự động:**
  * Job định kỳ `CampaignSchedulerJob` chạy vào 00:00 hằng ngày tự động Mở (`ACTIVE`) đợt đến ngày và Đóng (`CLOSED`) đợt hết hạn.
  * Tự động gửi email thông báo định dạng HTML qua `EmailService` tới toàn bộ sinh viên có tài khoản hoạt động khi đợt mở.
  * Hỗ trợ Admin/Mentor can thiệp thay đổi trạng thái thủ công (`PATCH /management/campaigns/{id}/status`).
* **APIs Đã Triển Khai:**
  * `GET /user/campaign/grouped`: Lấy danh sách đợt gom nhóm theo năm học (`academicYear DESC`), sắp xếp ưu tiên trạng thái (ACTIVE -> UPCOMING -> PAUSED -> CLOSED).
  * `GET /management/campaigns`: Danh sách đợt phân trang kèm bộ lọc chi tiết cho Admin/Mentor.
  * `PATCH /management/campaigns/{id}/status`: Cập nhật trạng thái đợt thủ công.

---

## 4. Feature: Nâng cấp Đa minh chứng (Multi-evidence per Criteria) - [COMPLETED]
* **Mục tiêu:** Cho phép sinh viên nộp nhiều minh chứng cho cùng 1 tiêu chí (thay vì ghi đè/xóa minh chứng cũ).
* **Cải tiến:**
  * Bỏ `@UniqueConstraint` trên `(user_id, criteria_id)` trong `Evidence.java`.
  * Hỗ trợ nộp thêm minh chứng mới hoặc cập nhật lại minh chứng cụ thể thông qua `evidenceId`.
  * Bổ sung API `DELETE /evidence/{id}` cho sinh viên xóa minh chứng khi chưa nộp hồ sơ.
  * Tích hợp cơ chế khóa minh chứng: Khi sinh viên đã nộp hồ sơ đợt (`SUBMITTED` / `APPROVED`), hệ thống chặn không cho tải/sửa/xóa minh chứng của đợt đó để bảo toàn dữ liệu đối soát.
  * `CriteriaDTO` và `ApplicationCriteriaDetailResponse` bổ sung danh sách `List<EvidenceItemDTO> evidences`.

---

## 5. Feature: Quản lý Hồ sơ xét tuyển (Application Management) - [COMPLETED]
* **Mục tiêu:** Quản lý hồ sơ hoàn chỉnh của sinh viên trong đợt xét tuyển, đánh giá tổng thể 5 tiêu chuẩn lớn và chốt công nhận danh hiệu.
* **Thuật toán Đánh giá Tiêu chuẩn (`EvaluationService`):**
  * Hỗ trợ 2 mục: **Tiêu chí bắt buộc (`isMandatory = true`)** phải đạt 100%; **Tiêu chí tự chọn (`isMandatory = false`)** chỉ cần đạt ít nhất 1 tiêu chí.
  * Tiêu chí đạt khi có ít nhất 1 minh chứng được duyệt (`APPROVED`) hoặc loại minh chứng là `NONE`.
  * Tiêu chuẩn đạt khi đạt toàn bộ tiêu chí bắt buộc VÀ đạt mục tiêu chí tự chọn.
* **Bảo toàn dữ liệu lịch sử:** Đóng băng toàn bộ lý lịch sinh viên tại thời điểm nộp thành bản ghi JSON `profileSnapshot` trong `Application`.
* **APIs Đã Triển Khai:**
  * **Sinh viên:**
    * `GET /application/preview/{campaignId}`: Xem trước tiến độ 5 tiêu chuẩn và kiểm tra điều kiện nộp.
    * `POST /application/submit`: Nộp hồ sơ chính thức (chỉ nộp khi đợt mở, profile completed, đạt 5/5 tiêu chuẩn).
    * `GET /application/my-application/{campaignId}`: Xem hồ sơ đã nộp của chính mình.
  * **Admin & Mentor:**
    * `GET /management/applications`: Bảng danh sách hồ sơ phân trang, lọc theo khoa, khóa, số tiêu chuẩn đạt, trạng thái.
    * `GET /management/applications/{id}`: Xem chi tiết toàn diện hồ sơ (lý lịch snapshot + cây 5 tiêu chuẩn kèm toàn bộ minh chứng + nhận xét thẩm định).
    * `PATCH /management/applications/{id}/evaluate`: Hội đồng chốt kết quả công nhận danh hiệu (`APPROVED` / `REJECTED`), nhận xét thẩm định là tùy chọn (không bắt buộc).
* **Kiểm thử tự động:** Đầy đủ Unit Tests cho `EvaluationServiceTest`, `ApplicationServiceTest`, `ApplicationManagementServiceTest` (100% Pass).
