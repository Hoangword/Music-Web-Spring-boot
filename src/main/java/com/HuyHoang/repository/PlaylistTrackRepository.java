package com.HuyHoang.repository;

import com.HuyHoang.Entity.PlaylistTracks;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistTrackRepository extends JpaRepository<PlaylistTracks, String> {
    List<PlaylistTracks> findByPlaylistId(String playlistId);

    Page<PlaylistTracks> findByPlaylistId(String playlistId,Pageable pageable);
}
