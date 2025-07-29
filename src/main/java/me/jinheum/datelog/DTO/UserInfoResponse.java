package me.jinheum.datelog.dto;

import java.util.UUID;

public record UserInfoResponse(
    UUID id,
    UUID userConnectionId,
    String userConnectionStatus,
    String userName,
    UUID partnerId,
    String partnerName,
    String partnerEmail,
    Boolean isSender
) {}