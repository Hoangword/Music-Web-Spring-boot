package com.HuyHoang.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class ListeningHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // Quan hệ với User
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ID bài hát từ Spotify
    @Column( nullable = false)
    private String spotifyTrackId;

    @Column( nullable = false)
    private LocalDateTime playedAt;
}
