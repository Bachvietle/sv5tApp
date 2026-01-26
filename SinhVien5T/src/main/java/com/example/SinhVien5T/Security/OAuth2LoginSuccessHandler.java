package com.example.SinhVien5T.Security;

import com.example.SinhVien5T.Entity.User;
import com.example.SinhVien5T.Entity.VerifyToken.RefreshToken;
import com.example.SinhVien5T.Repository.RefreshTokenRepository;
import com.example.SinhVien5T.Repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        // oAuth2User ở trong Object authentication
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // Lấy các ttin
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String avatar = oAuth2User.getAttribute("picture");

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = User.builder()
                    .email(email)
                    .userName(name)
                    .userPassword("") // login gg nên ko có pw
                    .avatar(avatar)
                    .isVerified(true) // gg đã verify email
                    .build();

            return userRepository.save(newUser);
        });

        // Tạo jwtToken như bth
        String accessToken = jwtService.generateAccessJwt(user);
        String refreshToken = jwtService.generateRefreshJwt(user, request);

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .id(UUID.randomUUID().toString())
                .token(refreshToken)
                .user(user)
                .expiredAt(LocalDateTime.now().plusDays(7))
                .ipAddress(jwtService.getIpAddress(request))
                .userAgent(request.getHeader("User-Agent"))
                .isRevoked(false)
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        // Vì đây là luồng redirect => ko thể trả về Json => gắn trực tiếp token vào param
        String redirectUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/oauth2/redirect")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toString();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);

    }
}
