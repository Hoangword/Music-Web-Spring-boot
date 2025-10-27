package com.HuyHoang.SpotifyService;

import com.HuyHoang.DTO.spotifyResponse.SearchTrackResponse;
import com.HuyHoang.DTO.spotifyResponse.SpotifyTrackResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SpotifyApiService {

    private final RestTemplate restTemplate;
    private final SpotifyAuthService spotifyAuthService;

    @Value("${spotify.api-base-url}")
    private String apiBaseUrl;

    public SpotifyTrackResponse getTrackInfo(String trackId) {
        String url = apiBaseUrl + "/tracks/" + trackId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + spotifyAuthService.getAccessToken());

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map body = response.getBody();
            return SpotifyTrackResponse.builder()
                    .trackName((String) body.get("name"))
                    .artistName(((Map)((Map)((java.util.List) body.get("artists")).get(0))).get("name").toString())
                    .albumImageUrl(((Map)((java.util.List)((Map) body.get("album")).get("images")).get(0)).get("url").toString())
                    .previewUrl((String) body.get("preview_url"))
                    .build();
        }
        throw new RuntimeException("Failed to fetch track info from Spotify");
    }



    public List<SearchTrackResponse> searchTrack(String keyword, int limit){
        String url = apiBaseUrl + "/search?q=" + keyword + "&type=track&limit=" + limit;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + spotifyAuthService.getAccessToken());

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map body = response.getBody();
            Map tracksObj = (Map) body.get("tracks");
            List<Map> items = (List<Map>) tracksObj.get("items");

            List<SearchTrackResponse> results = new ArrayList<>();
            for (Map item : items) {
                String trackId = (String) item.get("id");
                String trackName = (String) item.get("name");

                List<Map> artists = (List<Map>) item.get("artists");
                String artistName = artists.size() > 0 ? (String) artists.get(0).get("name") : "Unknown";

                Map album = (Map) item.get("album");
                List<Map> images = (List<Map>) album.get("images");
                String albumImageUrl = images.size() > 0 ? (String) images.get(0).get("url") : null;
                String previewUrl = (String) item.get("preview_url");
                results.add(SearchTrackResponse.builder()
                        .trackId(trackId)
                        .trackName(trackName)
                        .artistName(artistName)
                        .albumImageUrl(albumImageUrl)
                        .previewUrl(previewUrl)
                        .build());
            }
            return results;
        }
        throw new RuntimeException("Failed to search tracks on Spotify");
    }
}
