package com.HuyHoang.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"playlist_id", "spotify_track_id"}))
public class PlaylistTracks {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // Quan hệ với User
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "playlist_id", nullable = false)
    private Playlist playlist;

    // ID bài hát từ Spotify (không phải FK)
    @Column(name = "spotify_track_id",nullable = false)
    private String spotifyTrackId;
}
