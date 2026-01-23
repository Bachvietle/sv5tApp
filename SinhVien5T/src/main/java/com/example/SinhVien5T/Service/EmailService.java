package com.example.SinhVien5T.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Data
@RequiredArgsConstructor
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendVerifyRegisterMail(String verifyLink, String userMail) throws MessagingException {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper textSend = new MimeMessageHelper(message, true); // cho phép multipart

            textSend.setTo(userMail);
            textSend.setSubject("Xác minh tài khoản");
            textSend.setText("<p> Chào bạn,</p>" +
                    "<p> Vui lòng nhấn link sau đây để xác minh tài khoản:" +
                    "<a href=\"" + verifyLink + "\">Xác minh tài khoản</a>", true
            );

            mailSender.send(message);
        } catch (MessagingException e){
            throw new MessagingException("Ko gửi được link xác minh");
        }
    }
}
