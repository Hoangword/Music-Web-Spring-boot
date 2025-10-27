package com.HuyHoang.DTO.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListeningHistoryResponse {
    String id;
    String userId;
    String spotifyTrackId;
    LocalDateTime playedAt;
    String title;
    String artist;
    String albumImageUrl;
    String previewUrl;
}
