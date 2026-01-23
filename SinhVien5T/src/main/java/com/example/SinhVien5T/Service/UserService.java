package com.example.SinhVien5T.Service;

import com.example.SinhVien5T.Dto.Request.UserRegisterRequest;
import com.example.SinhVien5T.Entity.User;
import com.example.SinhVien5T.Entity.VerifyToken.RegisterVerifyToken;
import com.example.SinhVien5T.Repository.RegisterVerifyTokenRepository;
import com.example.SinhVien5T.Repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RegisterVerifyTokenRepository registerVerifyTokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public void register(UserRegisterRequest request) throws Exception {

        Optional<User> existUser = userRepository.findByEmail(request.getEmail());

        if (existUser.isPresent() && existUser.get().isVerified()) {
            throw new RuntimeException("Email đã được đăng kí");
        }

        User user = existUser.orElseGet(() ->
                User.builder()
                        .email(request.getEmail())
                        .build()
                );

        user.setUserName(request.getUserName());
        user.setUserPassword(passwordEncoder.encode(request.getUserPassword()));
        user.setVerified(false);

            // Xóa tất cả token cũ trước đó
            registerVerifyTokenRepository.deleteByUser(user);

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

                response.sendRedirect("http://localhost:8000/login?error=token_expired");
                return;
            }

            // Link đc xác minh thành công, save isActive User rồi redirect về trong login
            User user = registerVerifyToken.getUser();
            user.setVerified(true);
            userRepository.save(user);

            registerVerifyTokenRepository.delete(registerVerifyToken);

            response.sendRedirect("http://localhost:8000/login?verified=success");

        } catch (Exception e) {
            // Trường hợp lỗi khác (token rác, không tìm thấy...)
            response.sendRedirect("http://localhost:8000/login?error=invalid_token");
        }
        }
    }

