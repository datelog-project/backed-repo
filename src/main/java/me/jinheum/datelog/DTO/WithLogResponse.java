package me.jinheum.datelog.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import me.jinheum.datelog.entity.WithLog;

public record WithLogResponse(
    UUID id,
    LocalDate date,
    String placeName,
    String placeAddress,
    BigDecimal placeLat,
    BigDecimal placeLng,
    Integer feelingScore,
    String note,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<MediaResponse> mediaList
) {
    public static WithLogResponse from(WithLog log) {
        List<MediaResponse> mediaResponses = log.getMediaList().stream()
            .map(MediaResponse::from)
            .toList();
        return new WithLogResponse(
            log.getId(),
            log.getDate(),
            log.getPlaceName(),
            log.getPlaceAddress(),
            log.getPlaceLat(),
            log.getPlaceLng(),
            log.getFeelingScore(),
            log.getNote(),
            log.getCreatedAt(),
            log.getUpdatedAt(),
            mediaResponses
        );
    }
}
