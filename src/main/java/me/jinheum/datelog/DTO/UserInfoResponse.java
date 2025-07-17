package me.jinheum.datelog.dto;

import java.util.UUID;

public record UserInfoResponse(
    UUID id,
    UUID userConnectionId,
    String userConnectionStatus,
    UUID userId,
    UUID partnerId,
    String partnerName,
    String partnerEmail,
    Boolean isSender
) {}