package com.example.SinhVien5T.Security;

import com.example.SinhVien5T.Entity.User;
import com.example.SinhVien5T.Entity.VerifyToken.RefreshToken;
import com.example.SinhVien5T.Repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey jwtSecretKey;
    private final long accessExpiration;
    private final long refreshExpiration;
    public RefreshTokenRepository refreshTokenRepository;

    public JwtService(
            @Value("${app.jwt.secret}") String secretKey,
            @Value("${app.jwt.access-expiration}") long accessExpiration,
            @Value("${app.jwt.refresh-expiration}") long refreshExpiration,
            RefreshTokenRepository refreshTokenRepository
    ){
        // secretKey lưu dưới dạng BASE64 nên cần decode
        byte [] keyBytes = Decoders.BASE64.decode(secretKey);
        this.jwtSecretKey = Keys.hmacShaKeyFor(keyBytes);

        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public Claims extraAllClaims(String token){

        return Jwts.parser()
                .verifyWith(jwtSecretKey)
                .build()
                .parseSignedClaims(token) // Ktra chữ kí (Signature)
                                          // Ktra expiryDate
                .getPayload();
    }

    public String extractSubject(String token){
        return extraAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractSubject(token);

        // So sánh email trong token với email trong database VÀ kiểm tra token chưa hết hạn
        return (username.equals(userDetails.getUsername()));
    }

    public String generateAccessJwt(User user){

        return Jwts.builder()
                .subject(user.getEmail())
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpiration))
                .claim("role", user.getRole())
                .signWith(jwtSecretKey)
                .compact();
    }

    public String generateRefreshJwt(User user, HttpServletRequest request){

        String userAgent = request.getHeader("User-Agent");
        String ipAddress = getIpAddress(request);

        String refreshToken =  Jwts.builder()
                .subject(user.getEmail())
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .claim("role", user.getRole())
                .claim("ip", ipAddress)
                .compact();

        refreshTokenRepository.save(
                RefreshToken.builder()
                        .id(UUID.randomUUID().toString())
                        .user(user)
                        .token(refreshToken)
                        .expiredAt(LocalDateTime.now().plus(Duration.ofMillis(refreshExpiration)))
                        .ipAddress(ipAddress)
                        .userAgent(userAgent)
                        .build()
        );

        return refreshToken;
    }

    // helper để lấy ip (để đoạn code trên gọn hơn)
    public String getIpAddress (HttpServletRequest request){

        String remoteAddress = request.getHeader("X-Forwarded-For");

        return (remoteAddress != null && !remoteAddress.isEmpty()) ? remoteAddress : request.getRemoteAddr();
    }
}
