package com.example.SinhVien5T.auth.service;

import com.example.SinhVien5T.auth.dto.request.UserLoginRequest;
import com.example.SinhVien5T.auth.dto.request.UserRegisterRequest;
import com.example.SinhVien5T.auth.exception.InvalidEmailDomainException;
import com.example.SinhVien5T.auth.exception.InvalidTokenException;
import com.example.SinhVien5T.notification.service.EmailService;
import com.example.SinhVien5T.user.entity.User;
import com.example.SinhVien5T.user.entity.CustomUserDetails;
import com.example.SinhVien5T.auth.entity.RefreshToken;
import com.example.SinhVien5T.auth.entity.RegisterVerifyToken;
import com.example.SinhVien5T.user.exception.EmailExistException;
import com.example.SinhVien5T.auth.repository.OtpRepository;
import com.example.SinhVien5T.auth.repository.RefreshTokenRepository;
import com.example.SinhVien5T.auth.repository.RegisterVerifyTokenRepository;
import com.example.SinhVien5T.user.exception.UserNotFoundException;
import com.example.SinhVien5T.user.repository.UserRepository;
import com.example.SinhVien5T.common.security.JwtService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final EmailService emailService;
    private final OtpRepository otpRepository;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegisterVerifyTokenRepository registerVerifyTokenRepository;

    @Value("${app.auth.frontendUrl}")
    private String frontEndUrl;


    @Transactional
    public void register(@RequestBody UserRegisterRequest request) throws Exception {

        Optional<User> existUser = userRepository.findByEmail(request.getEmail());

        if (existUser.isPresent() && existUser.get().isVerified()) {
            throw new EmailExistException("Email đã được đăng kí");
        }


        if(!request.getEmail().toLowerCase().endsWith("@ms.hanu.edu.vn")){
            throw new InvalidEmailDomainException("Vui lòng sử dụng email nhà trường cấp (@ms.hanu.edu.vn)");
        }

        User user = existUser.orElseGet(() ->
                User.builder()
                        .email(request.getEmail())
                        .build()
        );

        user.setUserPassword(passwordEncoder.encode(request.getUserPassword()));
        user.setVerified(false);

        // Lưu vào db
        userRepository.save(user);

        // Xóa tất cả token cũ trước đó
        registerVerifyTokenRepository.deleteByUser(user);

        // Tạo link verify
        String token = UUID.randomUUID().toString();

        RegisterVerifyToken registerVerifyToken = RegisterVerifyToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(10))
                .build();

        registerVerifyTokenRepository.save(registerVerifyToken);

        String verifyLink = "http://localhost:8080/user/auth/verify_register_token?token=" + token; // BE handle endpoint nay (ko phai viet FE)

        emailService.sendVerifyRegisterMail(verifyLink, request.getEmail());

    }

    public String verifyRegisterToken(@RequestParam String token) {

        try {
            RegisterVerifyToken registerVerifyToken = registerVerifyTokenRepository.findByToken(token)
                    .orElseThrow(() -> new InvalidTokenException("Token không hợp lệ"));

            if (registerVerifyToken.getExpiryDate().isBefore(LocalDateTime.now())){

                registerVerifyTokenRepository.delete(registerVerifyToken);

                return frontEndUrl + "/login?error=token_expired";
            }

            // Link đc xác minh thành công, save isActive User rồi redirect về trong login
            User user = registerVerifyToken.getUser();
            user.setVerified(true);
            userRepository.save(user);

            registerVerifyTokenRepository.delete(registerVerifyToken);

            return frontEndUrl + "/login?verified=success";

        } catch (Exception e) {
            // Trường hợp lỗi khác (token rác, không tìm thấy...)
            return frontEndUrl + "/login?error=invalid_token";
        }
    }

    public Map<String, Object> login(UserLoginRequest userLoginRequest, String ipAddress, String userAgent) {

        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
                userLoginRequest.getEmail(), userLoginRequest.getUserPassword()
        );

        try{

            // 1. Xác minh user
            Authentication authentication = authenticationManager.authenticate(authRequest);

            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userRepository.findById(customUserDetails.getId())
                    .orElseThrow(() -> new UserNotFoundException("Tài khoản chưa được đăng kí"));

            if (!user.isVerified() || !user.isActive()){
                throw new UserNotFoundException("Tài khoản chưa được đăng kí");
            }


            // 2. Sau khi xác thực thành công, tạo token và cho user login
            String accessToken = jwtService.generateAccessJwt(user);
            String refreshToken = jwtService.generateRefreshJwt(user, ipAddress);

            // 4. Lưu refreshToken vào db
            RefreshToken rt = RefreshToken.builder()
                    .id(UUID.randomUUID().toString())
                    .token(refreshToken)
                    .user(user)
                    .expiredAt(LocalDateTime.now().plusDays(7))
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .build();

            refreshTokenRepository.save(rt);

            // 5. Trả accessToken về body reponse
            Map<String, Object> body = new HashMap<>();
            body.put("accessToken", accessToken);
            body.put("refreshToken", refreshToken); // Put here so Controller can extract and set to cookie
            body.put("user", Map.of(
                    "id", user.getId(), // Nên trả về ID để Frontend dùng
                    "email", user.getEmail(),
                    "role", user.getRole()
            ));

            return body;

        } catch (BadCredentialsException e){
            // Ném tiếp để GlobalHandler bắt (trả về 401)
            throw e;
        }
    }

    public void logOut(String refreshToken){
        if(refreshToken != null && !refreshToken.isEmpty()){
            refreshTokenRepository.deleteByToken(refreshToken);
        }
    }

    public void missingPassWord(String email) throws MessagingException {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new EmailExistException("Tài khoản không tồn tại")
        );

        String token = UUID.randomUUID().toString();

        RegisterVerifyToken resetToken = RegisterVerifyToken.builder()
                .user(user)
                .token(token)
                .expiryDate(LocalDateTime.now().plusMinutes(10))
                .build();

        registerVerifyTokenRepository.save(resetToken);

        String resetPwLink = frontEndUrl + "/reset_password?token=" + token;

        emailService.sendResetPwMail(resetPwLink, email);
    }

    public boolean verifyResetPwToken(@RequestParam String token) throws RuntimeException, IOException {

        try {
            RegisterVerifyToken registerVerifyToken = registerVerifyTokenRepository.findByToken(token)
                    .orElseThrow(() -> new RuntimeException("Token không hợp lệ"));

            if (registerVerifyToken.getExpiryDate().isBefore(LocalDateTime.now())){

                registerVerifyTokenRepository.delete(registerVerifyToken);

                return false;
            }

            return true;

        } catch (Exception e) {
            // Trường hợp lỗi khác (token rác, không tìm thấy...)
            return false;
        }
    }

    @Transactional
    public void resetPassWord(String token, String newPw) throws MessagingException {
        RegisterVerifyToken resetToken = registerVerifyTokenRepository.findByToken(token)
                        .orElseThrow(() -> new RuntimeException("Token không hợp lệ"));

        if(resetToken.getExpiryDate().isBefore(LocalDateTime.now())){
            registerVerifyTokenRepository.delete(resetToken);
            throw new RuntimeException("Token không hợp lệ");
        }

       registerVerifyTokenRepository.delete(resetToken);

        User user = resetToken.getUser();
        user.setUserPassword(passwordEncoder.encode(newPw));
        userRepository.save(user);
    }

    public Map<String, Object> refreshAccessToken(String refreshToken){

        if(refreshToken == null || refreshToken.isEmpty()) {
            throw new RuntimeException("Token ko hợp lệ");
        }

        RefreshToken storedRefreshToken = refreshTokenRepository.findByToken(refreshToken)
                .filter(rt -> rt.getExpiredAt().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new RuntimeException("Token ko hợp lệ"));

        String newAccessToken = jwtService.generateAccessJwt(storedRefreshToken.getUser());

        return Map.of("accessToken", newAccessToken);
    }
}


