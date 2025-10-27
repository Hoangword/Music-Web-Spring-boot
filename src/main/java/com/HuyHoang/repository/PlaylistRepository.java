package com.HuyHoang.repository;

import com.HuyHoang.DTO.response.PlaylistResponse;
import com.HuyHoang.Entity.Playlist;
import com.HuyHoang.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, String> {
    boolean existsByName(String playlistName);
    List<Playlist> findAllByUser(User user);

    Page<Playlist> findAll(Pageable pageable);

    Page<Playlist> findAllByUser(User user, Pageable pageable);
}
