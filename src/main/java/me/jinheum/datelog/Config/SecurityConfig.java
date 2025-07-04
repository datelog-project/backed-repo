package me.jinheum.datelog.config;

import me.jinheum.datelog.repository.UserAccountRepository;
import me.jinheum.datelog.security.CustomUserDetailsService;
import me.jinheum.datelog.security.JwtAuthenticationEntryPoint;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final UserAccountRepository userAccountRepository;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .authorizeHttpRequests(auth -> auth
        .requestMatchers("/auth/signin").permitAll()
        .requestMatchers("/auth/signup").permitAll()
        .requestMatchers("/reissue").permitAll()
        .anyRequest().authenticated())
        
        .exceptionHandling(exceptionHandling -> exceptionHandling
        .authenticationEntryPoint(jwtAuthenticationEntryPoint));
        
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new CustomUserDetailsService(userAccountRepository);
    }
}
