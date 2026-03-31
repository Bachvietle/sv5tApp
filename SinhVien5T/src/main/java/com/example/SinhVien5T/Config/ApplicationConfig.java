package com.example.SinhVien5T.Config;

import com.example.SinhVien5T.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor

// Tất cả các bean sẽ được khởi tạo tại đây
public class ApplicationConfig {

    private final UserRepository userRepository;

    @Bean
    public UserDetailsService userDetailsService(){

        return username -> userRepository.findByEmail(username).orElseThrow(
                () -> new RuntimeException("Email hoặc mật khẩu không chính xác")
        );
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    /*
    Tạo một DaoAuthenticationProvider
    để Spring biết cách xác thực User(lấy user từ db + pw)

    (việc xác thực user sẽ do DaoAuthenticationProvider đảm nhận)
     */
    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;

    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration autConfig) throws Exception {
        return autConfig.getAuthenticationManager();
    }
}
