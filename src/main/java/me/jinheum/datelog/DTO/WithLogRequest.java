package me.jinheum.datelog.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record WithLogRequest(
    LocalDate date,
    String placeName,
    String placeAddress,
    BigDecimal placeLat,
    BigDecimal placeLng,
    Integer feelingScore,
    String note
) {}