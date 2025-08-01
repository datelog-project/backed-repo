package me.jinheum.datelog.security;

import java.util.UUID;

import org.springframework.stereotype.Component;

import me.jinheum.datelog.entity.UserConnection;
import me.jinheum.datelog.exception.AccessDeniedException;

@Component
public class WithLogValidator {
    public void validateUserInConnection(UserConnection connection, UUID userId) {
        if (!connection.getUser().getId().equals(userId) &&
            !connection.getPartner().getId().equals(userId)) {
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }
    }
}
