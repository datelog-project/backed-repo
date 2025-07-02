package me.jinheum.datelog.DTO;

import java.util.UUID;

public record SigninResponse(
    UUID id,
    String username,
    String accessToken
) {}
