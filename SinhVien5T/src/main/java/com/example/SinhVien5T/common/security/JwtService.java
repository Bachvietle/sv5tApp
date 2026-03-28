package com.example.SinhVien5T.common.security;

import com.example.SinhVien5T.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Service
@Data
public class JwtService {

    private final SecretKey jwtSecretKey;
    private final long accessExpiration;
    private final long refreshExpiration;

    public JwtService(
            @Value("${app.jwt.secret}") String secretKey,
            @Value("${app.jwt.access-expiration}") long accessExpiration,
            @Value("${app.jwt.refresh-expiration}") long refreshExpiration
    ){
        // secretKey lưu dưới dạng BASE64 nên cần decode
        byte [] keyBytes = Decoders.BASE64.decode(secretKey);
        this.jwtSecretKey = Keys.hmacShaKeyFor(keyBytes);

        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
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
                .claim("role", user.getRole().toString())
                .claim("userId", user.getId())
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
                .claim("role", user.getRole().toString())
                .claim("ip", ipAddress)
                .compact();

        return refreshToken;
    }

    // helper để lấy ip (để đoạn code trên gọn hơn)
    public String getIpAddress (HttpServletRequest request){

        String remoteAddress = request.getHeader("X-Forwarded-For");

        return (remoteAddress != null && !remoteAddress.isEmpty()) ? remoteAddress : request.getRemoteAddr();
    }
}


