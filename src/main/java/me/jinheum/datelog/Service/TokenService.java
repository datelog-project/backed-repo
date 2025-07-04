package me.jinheum.datelog.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final StringRedisTemplate redisTemplate;
    private final Duration refreshTokenValidity = Duration.ofDays(7);

    private static final String REFRESH_TOKEN_PREFIX = "refreshToken:";

    private String buildKey(UUID userId) {
        return REFRESH_TOKEN_PREFIX + userId.toString();
    }

    public void saveRefreshToken(UUID userId, String refreshToken) {
        String key = buildKey(userId);
        redisTemplate.opsForValue().set(key, refreshToken, refreshTokenValidity);
    }

    public String getRefreshToken(UUID userId) {
        return redisTemplate.opsForValue().get(buildKey(userId));
    }

    public void deleteRefreshToken(UUID userId) {
        redisTemplate.delete(buildKey(userId));
    }

    public boolean isRefreshTokenValid(UUID userId, String refreshToken) {
        String storedToken = getRefreshToken(userId);
        return storedToken != null && storedToken.equals(refreshToken);
    }
}
