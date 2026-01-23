package com.example.SinhVien5T.Service;

import com.example.SinhVien5T.Dto.Request.UserRegisterRequest;
import com.example.SinhVien5T.Entity.User;
import com.example.SinhVien5T.Entity.VerifyToken.RegisterVerifyToken;
import com.example.SinhVien5T.Repository.RegisterVerifyTokenRepository;
import com.example.SinhVien5T.Repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RegisterVerifyTokenRepository registerVerifyTokenRepository;
    private final EmailService emailService;

    public void register(UserRegisterRequest request) throws Exception {

        if(userRepository.existsByEmail(request.getEmail())){
            throw new Exception("Email đã được đăng kí");
        }

        // Tạo user mới với isActive = false
        User user = User.builder()
                .email(request.getEmail())
                .userName(request.getUserName())
                .userPassword(request.getUserPassword())
                .isActive(false)
                .build();

        // Lưu vào db
        userRepository.save(user);

        // Tạo link verify
        String token = UUID.randomUUID().toString();

        RegisterVerifyToken registerVerifyToken = RegisterVerifyToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(10))
                .build();

        registerVerifyTokenRepository.save(registerVerifyToken);

        String verifyLink = "http://localhost:8080/user/verify_register_token?token=" + token;

        emailService.sendVerifyRegisterMail(verifyLink, request.getEmail());

    }

    public void verifyRegisterToken(String token, HttpServletResponse response) throws RuntimeException, IOException {

        try {
            RegisterVerifyToken registerVerifyToken = registerVerifyTokenRepository.findByToken(token)
                    .orElseThrow(() -> new RuntimeException("Token không hợp lệ"));

            if (registerVerifyToken.getExpiryDate().isBefore(LocalDateTime.now())){

                registerVerifyTokenRepository.delete(registerVerifyToken);

                response.sendRedirect("http://localhost:5173/login?error=token_expired");
                return;
            }

            // Link đc xác minh thành công, save isActive User rồi redirect về trong login
            User user = registerVerifyToken.getUser();
            user.setActive(true);
            userRepository.save(user);

            registerVerifyTokenRepository.delete(registerVerifyToken);

            response.sendRedirect("http://localhost:5173/login?verified=success");

        } catch (Exception e) {
            // Trường hợp lỗi khác (token rác, không tìm thấy...)
            response.sendRedirect("http://localhost:5173/login?error=invalid_token");
        }
        }
    }

