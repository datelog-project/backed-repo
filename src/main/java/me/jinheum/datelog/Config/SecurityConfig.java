package me.jinheum.datelog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;
import me.jinheum.datelog.security.JwtProvider;
import me.jinheum.datelog.security.SignoutFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final StringRedisTemplate redisTemplate;
    private final JwtProvider jwtProvider;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
        .requestMatchers("/auth/signin").permitAll()
        .requestMatchers("/auth/signup").permitAll()
        .requestMatchers("/reissue").permitAll()
        .anyRequest().authenticated())
        .addFilterBefore(signoutFilter(), UsernamePasswordAuthenticationFilter.class); //로그인 처리보다 앞에서 처리함
        return http.build();
    }

    @Bean
    public SignoutFilter signoutFilter() {
        return new SignoutFilter(redisTemplate, jwtProvider);
    }
}
