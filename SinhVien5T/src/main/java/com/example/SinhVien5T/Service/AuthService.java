package com.example.SinhVien5T.Service;

import com.example.SinhVien5T.Dto.Request.UserLoginRequest;
import com.example.SinhVien5T.Dto.Request.UserRegisterRequest;
import com.example.SinhVien5T.Entity.User;
import com.example.SinhVien5T.Entity.VerifyToken.Otp;
import com.example.SinhVien5T.Entity.VerifyToken.RefreshToken;
import com.example.SinhVien5T.Entity.VerifyToken.RegisterVerifyToken;
import com.example.SinhVien5T.Exception.EmailExistException;
import com.example.SinhVien5T.Repository.OtpRepository;
import com.example.SinhVien5T.Repository.RefreshTokenRepository;
import com.example.SinhVien5T.Repository.RegisterVerifyTokenRepository;
import com.example.SinhVien5T.Repository.UserRepository;
import com.example.SinhVien5T.Security.JwtService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
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
            throw new EmailExistException("Tài khoản đã tồn tại");
        }


        if(!request.getEmail().toLowerCase().endsWith("@ms.hanu.edu.vn")){
            throw new RuntimeException("Vui lòng sử dụng email nhà trường cấp (@ms.hanu.edu.vn)");
        }

        User user = existUser.orElseGet(() ->
                User.builder()
                        .email(request.getEmail())
                        .build()
        );

        user.setUserName(request.getUserName());
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

        String verifyLink = "https://tangiest-rosenda-stirruplike.ngrok-free.dev/user/auth/verify_register_token?token=" + token; // BE handle endpoint nay (ko phai viet FE)

        emailService.sendVerifyRegisterMail(verifyLink, request.getEmail());

    }

    public void verifyRegisterToken(@RequestParam String token, HttpServletResponse response) throws RuntimeException, IOException {

        try {
            RegisterVerifyToken registerVerifyToken = registerVerifyTokenRepository.findByToken(token)
                    .orElseThrow(() -> new RuntimeException("Token không hợp lệ"));

            if (registerVerifyToken.getExpiryDate().isBefore(LocalDateTime.now())){

                registerVerifyTokenRepository.delete(registerVerifyToken);

                response.sendRedirect( frontEndUrl + "/login?error=token_expired");
                return;
            }

            // Link đc xác minh thành công, save isActive User rồi redirect về trong login
            User user = registerVerifyToken.getUser();
            user.setVerified(true);
            userRepository.save(user);

            registerVerifyTokenRepository.delete(registerVerifyToken);

            response.sendRedirect(frontEndUrl + "/login?verified=success");

        } catch (Exception e) {
            // Trường hợp lỗi khác (token rác, không tìm thấy...)
            response.sendRedirect(frontEndUrl + "/login?error=invalid_token");
        }
    }

    public Map<String, Object> login(UserLoginRequest userLoginRequest, HttpServletRequest request, HttpServletResponse response) throws MessagingException {

        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
                userLoginRequest.getEmail(), userLoginRequest.getUserPassword()
        );

        try{

            // 1. Xác minh user
            Authentication authentication = authenticationManager.authenticate(authRequest);

            User user = (User) authentication.getPrincipal();


            // 2. Sau khi xác thực thành công, tạo token và cho user login
            String accessToken = jwtService.generateAccessJwt(user);
            String refreshToken = jwtService.generateRefreshJwt(user, request);

        /*
        refreshToken sẽ được đưa vào cooke rồi gắn vào header của reponse
        accessToken đuợc đưa lên controller rồi trả về trong Body reponse
         */

            // 3. add refresh to Cookie
            addRefreshCookie(refreshToken, 7 * 24 * 60 * 60, response);

            // 4. Lưu refreshToken vào db
            RefreshToken rt = RefreshToken.builder()
                    .id(UUID.randomUUID().toString())
                    .token(refreshToken)
                    .user(user)
                    .expiredAt(LocalDateTime.now().plusDays(7))
                    .ipAddress(jwtService.getIpAddress(request))
                    .userAgent(request.getHeader("User-Agent"))
                    .build();

            refreshTokenRepository.save(rt);

            // 5. Trả accessToken về body reponse
            Map<String, Object> body = new HashMap<>();
            body.put("accessToken", accessToken);
            body.put("user", Map.of(
                    "id", user.getId(), // Nên trả về ID để Frontend dùng
                    "email", user.getEmail()
            ));

            return body;

        } catch (BadCredentialsException e){
            // Ném tiếp để GlobalHandler bắt (trả về 401)
            throw e;
        }
    }

    public void logOut(HttpServletRequest request, HttpServletResponse response){

        String refreshToken = getValueCookie("refreshToken", request);

        if(refreshToken != null){
            refreshTokenRepository.deleteByToken(refreshToken);
        }

        // Xóa cookie trong trình duyệt
        addRefreshCookie(null, 0, response);
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

    public Map<String, Object> refreshAccessToken(HttpServletRequest request){

        String refreshToken = getValueCookie("refreshToken", request);

        RefreshToken storedRefreshToken = refreshTokenRepository.findByToken(refreshToken)
                .filter(rt -> rt.getExpiredAt().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ"));

        String newAccessToken = jwtService.generateAccessJwt(storedRefreshToken.getUser());

        return Map.of("accessToken", newAccessToken);
    }



    /*
    .......................................................................
     Các hàm Helper
     */

    // add RefreshToken to Cookie
    public void addRefreshCookie(String refreshToken, int maxAge, HttpServletResponse response) {

        // Tạo cookie để cho refreshToken vào
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true) // Cookie sẽ không thể bị truy cập bởi JavaScript thông qua document.cookie secure
                .secure(false) // để tạm dùng trong MT dev (chạy local dùng http)
                .path("/")
                .maxAge(maxAge)
                .sameSite("Strict") // để tạm dùng trong MT dev
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }


    /*
     Hàm này để lấy giá trị của name từ cookie.

     (Ở đây sẽ dùng để lấy gtri của refreshToken)
     */
    public String getValueCookie(String name, HttpServletRequest request){

        if(request.getCookies() == null){
            return null;
        }

        return Arrays.stream(request.getCookies()) // Gtri trả về là 1 Array
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst().orElse(null);

    }
}
