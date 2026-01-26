package com.example.SinhVien5T.Security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader == null && !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }

        try{
            jwt = authHeader.substring(7);

            Claims claims = jwtService.extraAllClaims(jwt);
            userEmail = claims.getSubject();

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null){

                String role = claims.get("role", String.class);

                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("role:" + role);

                /*
                Dựng đối tượng User nhanh

                Giải thích: Vì ta chỉ cần các thông tin về username (email), role để dựng UsernamePasswordAuthenticationToken
                mà các ttin này đã có luôn tỏng jwt.

                => Chỉ cần extract các ttin này từ jwt mà ko cần query xuống db để lấy UserDetails mỗi lần reqest
                 */
                UserDetails userDetails = new User(
                        userEmail,
                        "",
                        Collections.singletonList(authority)
                );

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                );

                /*
                Method buildDetails extract thông tin từ request:
                - Remote address: Địa chỉ IP của client.
                - Session ID: ID của session HTTP nếu tồn tại (giúp track session).

                Rồi gắn ttin này vào authToken
                */
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set authentication mới vào contextholder
                SecurityContextHolder.getContext().setAuthentication(authToken);
                }

        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}" + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
