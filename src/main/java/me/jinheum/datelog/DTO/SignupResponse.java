package me.jinheum.datelog.DTO;

import java.util.UUID;

public record SignupResponse(
    UUID id,
    String username
) {}
