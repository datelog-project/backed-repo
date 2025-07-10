package me.jinheum.datelog.dto;

import java.util.UUID;

public record InviteResponse(String message, UUID connectionId) {}