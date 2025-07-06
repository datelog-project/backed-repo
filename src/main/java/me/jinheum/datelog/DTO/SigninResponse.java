package me.jinheum.datelog.dto;

import java.util.UUID;

public record SigninResponse(
    UUID id,
    String accessToken
) {}
