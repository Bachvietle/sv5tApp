package com.example.SinhVien5T.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Data
@RequiredArgsConstructor
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendVerifyRegisterMail(String verifyLink, String userMail) throws MessagingException {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper textSend = new MimeMessageHelper(message, true); // cho phép multipart

            textSend.setTo(userMail);
            textSend.setSubject("Xác minh tài khoản");
            textSend.setText("<p> Chào bạn,</p>" +
                    "<p> Vui lòng nhấn link sau đây để xác minh tài khoản:" +
                    "<a href=\"" + verifyLink + "\">Xác minh tài khoản</a>", true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new MessagingException("Ko gửi được link xác minh");
        }
    }


    public void sendVerifyLoginMail(String otp, String userMail) throws MessagingException {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper textSend = new MimeMessageHelper(message, true);

            textSend.setTo(userMail);
            textSend.setSubject("Mã OTP xác minh tài khoản");

            String htmlContent = """
                        <div style="font-family: Arial, sans-serif; padding: 20px; border: 1px solid #ddd; border-radius: 10px;">
                            <h2 style="color: #1a73e8;">Xác thực đăng nhập</h2>
                            <p>Chào bạn,</p>
                            <p>Dưới đây là mã xác minh (OTP) của bạn. Mã này sẽ hết hạn sau 5 phút:</p>

                            <h1 style="color: #333; letter-spacing: 5px; font-size: 32px;">%s</h1>

                            <p>Vui lòng không chia sẻ mã này cho bất kỳ ai.</p>
                            <hr style="border: none; border-top: 1px solid #eee;" />
                            <p style="font-size: 12px; color: #888;">Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này.</p>
                        </div>
                    """
                    .formatted(otp); // Truyền OTP vào %s

            textSend.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new MessagingException("Ko gửi được email OTP");
        }
    }

    public void sendResetPwMail(String resetPwLink, String userMail) throws MessagingException {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper textSend = new MimeMessageHelper(message, true); // cho phép multipart

            textSend.setTo(userMail);
            textSend.setSubject("Đặt lại mật khẩu");
            textSend.setText("<p> Chào bạn,</p>" +
                    "<p> Vui lòng nhấn link sau đây để đặt lại mật khẩu:" +
                    "<a href=\"" + resetPwLink + "\">Đặt lại mật khẩu</a>", true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new MessagingException("Ko gửi được link xác minh");
        }
    }

    @Async
    public void sendCampaignAnnouncementToStudents(
            String campaignName,
            String level,
            Long academicYear,
            LocalDate startDate,
            LocalDate endDate,
            List<String> recipientEmails) {

        if (recipientEmails == null || recipientEmails.isEmpty()) {
            return;
        }

        String startDateStr = startDate != null ? startDate.toString() : "Chưa xác định";
        String endDateStr = endDate != null ? endDate.toString() : "Chưa xác định";
        String academicYearStr = academicYear != null ? String.valueOf(academicYear) : "N/A";
        String levelStr = level != null ? level : "N/A";

        String htmlContent = """
            <div style="font-family: Arial, sans-serif; padding: 24px; border: 1px solid #e0e0e0; border-radius: 8px; max-width: 650px; margin: 0 auto; color: #333;">
                <div style="text-align: center; border-bottom: 2px solid #1a73e8; padding-bottom: 12px; margin-bottom: 20px;">
                    <h2 style="color: #1a73e8; margin: 0;">DANH HIỆU SINH VIÊN 5 TỐT</h2>
                    <p style="color: #555; margin: 5px 0 0 0; font-size: 14px;">Thông báo mở cổng tiếp nhận hồ sơ xét tuyển chính thức</p>
                </div>
                <p>Chào bạn,</p>
                <p>Hệ thống trân trọng thông báo: <strong>%s</strong> đã chính thức mở cổng tiếp nhận hồ sơ xét duyệt danh hiệu Sinh viên 5 Tốt.</p>
                
                <div style="background-color: #f8f9fa; border-left: 4px solid #1a73e8; padding: 14px 18px; margin: 18px 0; border-radius: 4px;">
                    <p style="margin: 4px 0;"><strong>• Cấp xét tuyển:</strong> %s</p>
                    <p style="margin: 4px 0;"><strong>• Năm học:</strong> %s</p>
                    <p style="margin: 4px 0;"><strong>• Thời gian mở cổng:</strong> Từ ngày %s</p>
                    <p style="margin: 4px 0; color: #d93025;"><strong>• Hạn chót nộp hồ sơ:</strong> Hết ngày %s (23:59)</p>
                </div>

                <h4 style="color: #1a73e8; margin-bottom: 8px;">Lời nhắc quan trọng dành cho sinh viên:</h4>
                <ul style="padding-left: 20px; line-height: 1.6; margin-top: 4px;">
                    <li>Kiểm tra và hoàn thiện minh chứng cho cả <strong>5 Tiêu chuẩn lớn</strong> (Đạo đức tốt, Học tập tốt, Thể lực tốt, Tình nguyện tốt, Hội nhập tốt).</li>
                    <li>Đảm bảo các minh chứng yêu cầu đã được Mentor/Admin thẩm định Đạt (Approved).</li>
                    <li>Sau khi hoàn tất, hãy truy cập hệ thống và bấm <strong>"Nộp hồ sơ chính thức"</strong> trước thời hạn quy định.</li>
                </ul>

                <p style="margin-top: 24px; font-size: 13px; color: #666; border-top: 1px solid #eee; padding-top: 12px;">
                    Email này được gửi tự động từ Hệ thống Quản lý và Xét duyệt Sinh viên 5 Tốt. Chúc các bạn đạt kết quả xuất sắc!
                </p>
            </div>
            """.formatted(campaignName, levelStr, academicYearStr, startDateStr, endDateStr);

        for (String email : recipientEmails) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setTo(email);
                helper.setSubject("[Sinh viên 5 Tốt] Thông báo mở đợt xét tuyển: " + campaignName);
                helper.setText(htmlContent, true);
                mailSender.send(message);
            } catch (Exception e) {
                // Log and continue sending to remaining recipients
                System.err.println("Không gửi được email mở đợt tới: " + email + " - Lỗi: " + e.getMessage());
            }
        }
    }
}


