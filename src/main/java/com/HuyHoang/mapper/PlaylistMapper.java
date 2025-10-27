package com.HuyHoang.mapper;

import com.HuyHoang.DTO.request.PlaylistCreationRequest;
import com.HuyHoang.DTO.request.PlaylistUpdateRequest;
import com.HuyHoang.DTO.response.PlaylistResponse;
import com.HuyHoang.Entity.Playlist;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PlaylistMapper {
    @Mapping(target = "user", ignore = true)
    Playlist toPlaylist(PlaylistCreationRequest request);
    PlaylistResponse toPlaylistResponse(Playlist playlist);
    void updatePlaylist(@MappingTarget Playlist playlist, PlaylistUpdateRequest request);
}
