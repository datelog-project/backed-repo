package me.jinheum.datelog.dto;

import java.time.LocalDate;

public record WithLogRequest(
    LocalDate date,
    String placeName,
    Integer feelingScore,
    String note
) {}