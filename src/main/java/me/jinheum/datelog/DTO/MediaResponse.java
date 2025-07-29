package me.jinheum.datelog.dto;

import java.util.UUID;

import me.jinheum.datelog.entity.Media;

public record MediaResponse(
    UUID id,
    String mediaUrl,
    String mediaType
) {
    public static MediaResponse from(Media media) {
        return new MediaResponse(
            media.getId(),
            media.getMediaUrl(),
            media.getMediaType()
        );
    }
}