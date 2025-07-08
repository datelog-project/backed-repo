package me.jinheum.datelog.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "jwt")
@RequiredArgsConstructor
@Getter
@Setter
public class JwtProperties {

    private int accessTokenValidityMinutes;
    private int refreshTokenValidityDays;
    private String secret;
    
    public Duration getAccessTokenValidity() {
        return Duration.ofMinutes(accessTokenValidityMinutes);
    }

    public Duration getRefreshTokenValidity() {
        return Duration.ofDays(refreshTokenValidityDays);
    }

    public String getSecret() {
        return secret;
    }
}
