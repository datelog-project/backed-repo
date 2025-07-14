package me.jinheum.datelog.dto;

import java.util.UUID;

public record UserInfoResponse(
    UUID id,
    UUID userConnectionId,
    String userConnectionStatus
) {}