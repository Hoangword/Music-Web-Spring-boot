package com.HuyHoang.service;

import com.HuyHoang.DTO.request.PlaylistTrackRequest;
import com.HuyHoang.DTO.response.PlaylistTrackResponse;
import com.HuyHoang.Entity.Playlist;
import com.HuyHoang.Entity.PlaylistTracks;
import com.HuyHoang.SpotifyService.SpotifyApiService;
import com.HuyHoang.exception.AppException;
import com.HuyHoang.exception.ErrorCode;
import com.HuyHoang.mapper.PlaylistTrackMapper;
import com.HuyHoang.repository.PlaylistRepository;
import com.HuyHoang.repository.PlaylistTrackRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlaylistTracksService {
    PlaylistTrackRepository playlistTrackRepository;
    PlaylistRepository playlistRepository;
    PlaylistTrackMapper playlistTrackMapper;
    SpotifyApiService spotifyApiService;



    public PlaylistTrackResponse addTrackToPlaylist(PlaylistTrackRequest request){
        Playlist playlist = playlistRepository.findById(request.getPlaylistId())
                .orElseThrow(() -> new AppException(ErrorCode.PLAYLIST_NOT_EXISTED));
        //PlaylistTracks playlistTracks = playlistTrackMapper.toPlaylistTracks(request);
        PlaylistTracks playlistTracks = PlaylistTracks.builder()
                .spotifyTrackId(request.getTrackId())
                .playlist(playlist)
                .user(playlist.getUser())
                .build();
        //playlistTrackRepository.save(playlistTracks);

        return playlistTrackMapper.toPlaylistTrackResponse(
                playlistTrackRepository.save(playlistTracks)
        );
    }

    public void deletedTrackFromPlaylist(String playlistTrackId){
        playlistTrackRepository.deleteById(playlistTrackId);
    }

    public List<PlaylistTrackResponse> getAllTrackFromPlaylist(String playlistId){


        return playlistTrackRepository.findByPlaylistId(playlistId).stream().map(
                track -> {
                    var spotifyInfo = spotifyApiService.getTrackInfo(track.getSpotifyTrackId());
                   return PlaylistTrackResponse.builder()
                            .id(track.getId())
                            .playlistId(track.getPlaylist().getId())
                            .trackId(track.getSpotifyTrackId())
                            .trackName(spotifyInfo.getTrackName())
                            .artistName(spotifyInfo.getArtistName())
                            .albumImageUrl(spotifyInfo.getAlbumImageUrl())
                           .previewUrl(spotifyInfo.getPreviewUrl())
                            .build();

                }
        ).toList();
       // return  playlistTrackRepository.findByPlaylistId(playlistId).stream().map(track -> playlistTrackMapper.toPlaylistTrackResponse(track)).toList();
    }

    public Page<PlaylistTrackResponse> getAllPlaylistTracksPaged(String playlistId, int page, int size, String direction){



        Pageable pageable = PageRequest.of(page,size);
        return playlistTrackRepository.findByPlaylistId(playlistId, pageable).map( track -> {
            var spotifyInfo = spotifyApiService.getTrackInfo(track.getSpotifyTrackId());
            return PlaylistTrackResponse.builder()
                    .id(track.getId())
                    .playlistId(track.getPlaylist().getId())
                    .trackId(track.getSpotifyTrackId())
                    .trackName(spotifyInfo.getTrackName())
                    .artistName(spotifyInfo.getArtistName())
                    .albumImageUrl(spotifyInfo.getAlbumImageUrl())
                    .previewUrl(spotifyInfo.getPreviewUrl())
                    .build();
            }
        );
    }
}
