package me.jinheum.datelog.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import me.jinheum.datelog.entity.Media;
import me.jinheum.datelog.entity.WithLog;

public record WithLogPreviewResponse(
    UUID id,
    String placeName,
    String placeAddress,
    BigDecimal placeLat,
    BigDecimal placeLng,
    LocalDate date,
    String thumbnailUrl,
    String previewNote,
    Long cost,
    Integer feelingScore
) {
    public static WithLogPreviewResponse from(WithLog log) {
        String thumbnail = log.getMediaList().stream()
            .filter(m -> m.getMediaType().equalsIgnoreCase("IMAGE"))
            .map(Media::getMediaUrl)
            .findFirst()
            .orElse(null);

        return new WithLogPreviewResponse(
            log.getId(),
            log.getPlaceName(),
            log.getPlaceAddress(),
            log.getPlaceLat(),
            log.getPlaceLng(),
            log.getDate(),
            thumbnail,
            log.getNote() != null && log.getNote().length() > 20
                ? log.getNote().substring(0, 20) + "..."
                : log.getNote(),
            log.getCost(),
            log.getFeelingScore()
        );
    }
}