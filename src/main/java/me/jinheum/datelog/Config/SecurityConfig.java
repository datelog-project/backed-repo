package me.jinheum.datelog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;
import me.jinheum.datelog.security.JwtAuthenticationFilter;
import me.jinheum.datelog.security.JwtProvider;
import me.jinheum.datelog.security.SignoutFilter;
import me.jinheum.datelog.service.TokenService;
import me.jinheum.datelog.util.CookieUtil;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final TokenService tokenService;
    private final JwtProvider jwtProvider;
    private final CookieUtil cookieUtil;


    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
        .requestMatchers(HttpMethod.POST, "/auth/signin").permitAll()
        .requestMatchers(HttpMethod.POST, "/auth/signup").permitAll()
        .requestMatchers(HttpMethod.POST, "/auth/reissue").permitAll()
        .requestMatchers(HttpMethod.GET, "/share/**").permitAll()
        .requestMatchers(
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/webjars/**",
            "/error",
            "/.well-known/**"
        ).permitAll()
        .anyRequest().authenticated())
        .addFilterBefore(signoutFilter(), UsernamePasswordAuthenticationFilter.class)//로그인 처리보다 앞에서 처리함
        .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public SignoutFilter signoutFilter() {
        return new SignoutFilter(jwtProvider,tokenService, cookieUtil);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtProvider);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
