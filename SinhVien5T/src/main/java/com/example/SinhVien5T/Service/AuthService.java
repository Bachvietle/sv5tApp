package com.example.SinhVien5T.Service;

import com.example.SinhVien5T.Dto.Request.UserLoginRequest;
import com.example.SinhVien5T.Entity.User;
import com.example.SinhVien5T.Entity.VerifyToken.Otp;
import com.example.SinhVien5T.Entity.VerifyToken.RefreshToken;
import com.example.SinhVien5T.Repository.OtpRepository;
import com.example.SinhVien5T.Repository.RefreshTokenRepository;
import com.example.SinhVien5T.Repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final EmailService emailService;
    private final OtpRepository otpRepository;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    public void login(UserLoginRequest userLoginRequest) throws MessagingException {

        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
                userLoginRequest.getEmail(), userLoginRequest.getUserPassword()
        );

        try{
            Authentication authentication = authenticationManager.authenticate(authRequest);

            User user = (User) authentication.getPrincipal();

            // Tạo otp với 6 chữ số
            String otp = String.valueOf((int) (Math.random() * 900000) + 100000);

            Otp newOtp = Otp.builder()
                    .id(UUID.randomUUID().toString())
                    .email(user.getEmail())
                    .otp(otp)
                    .expiredAt(LocalDateTime.now().plusMinutes(5))
                    .build();

            otpRepository.save(newOtp);

            emailService.sendVerifyLoginMail(otp, user.getEmail());

        } catch (DisabledException e){
            // Ném tiếp để GlobalHandler bắt (trả về 403)
            throw e;
        } catch (BadCredentialsException e){
            // Ném tiếp để GlobalHandler bắt (trả về 401)
            throw e;
        }
    }

    public Map<String, Object> verifyOtpLogin(String otp, HttpServletRequest request, HttpServletResponse response){
        Otp checkOtp = otpRepository.findByOtp(otp).orElseThrow(
                () -> new RuntimeException("Otp không hợp lệ hoặc đã hết hạn")
        );

        if(checkOtp.getExpiredAt().isBefore(LocalDateTime.now())){
            throw new RuntimeException("Otp không hợp lệ hoặc đã hết hạn");
        }

        User user = userRepository.findByEmail(checkOtp.getEmail()).orElseThrow(
                () -> new RuntimeException("Ko tìm thấy email")
        );

        // Xóa otp ngay khi xác thực xong
        otpRepository.delete(checkOtp);

        // Sau khi xác thực otp thành công, tạo token và cho user login
        String accessToken = jwtService.generateAccessJwt(user);
        String refreshToken = jwtService.generateRefreshJwt(user, request);

        /*
        refreshToken sẽ được đưa vào cooke rồi gắn vào header của reponse
        accessToken đuợc đưa lên controller rồi trả về trong Body reponse
         */

        // Tạo cookie để cho refreshToken vào
        ResponseCookie cookie = ResponseCookie.from(refreshToken)
                .httpOnly(true) // Cookie sẽ không thể bị truy cập bởi JavaScript thông qua document.cookie secure
                .secure(false) // để tạm dùng trong MT dev (chạy local dùng http)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Strict") // để tạm dùng trong MT dev
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // Lưu refreshToken vào db
        RefreshToken rt = RefreshToken.builder()
                .id(UUID.randomUUID().toString())
                .token(refreshToken)
                .user(user)
                .expiredAt(LocalDateTime.now().plusDays(7))
                .ipAddress(jwtService.getIpAddress(request))
                .userAgent(request.getHeader("User-Agent"))
                .build();

        refreshTokenRepository.save(rt);

        // Trả accessToken về body reponse
        Map<String, Object> body = new HashMap<>();

        body.put("accessToken", accessToken);
        body.put("user", Map.of(
                "email", user.getEmail(),
                "userName", user.getUserName()
        ));

        return body;
    }

    public void loginGoogle(){}
}
