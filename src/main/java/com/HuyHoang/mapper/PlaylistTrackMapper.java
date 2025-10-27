package com.HuyHoang.mapper;

import com.HuyHoang.DTO.request.PlaylistTrackRequest;
import com.HuyHoang.DTO.response.PlaylistTrackResponse;
import com.HuyHoang.Entity.PlaylistTracks;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PlaylistTrackMapper {
    PlaylistTracks toPlaylistTracks(PlaylistTrackRequest request);


    @Mapping(source = "playlist.id", target = "playlistId")
    @Mapping(source = "spotifyTrackId", target = "trackId")
    PlaylistTrackResponse toPlaylistTrackResponse(PlaylistTracks playlistTracks);
}
