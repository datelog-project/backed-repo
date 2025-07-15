package me.jinheum.datelog.dto;

import java.util.UUID;

import me.jinheum.datelog.entity.enums.ConnectionStatus;

public record ConnectionStatusDto(
    UUID connectionId,
    ConnectionStatus status
) {}
