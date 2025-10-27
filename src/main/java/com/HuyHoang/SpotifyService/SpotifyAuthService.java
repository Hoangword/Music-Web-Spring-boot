package com.HuyHoang.SpotifyService;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.http.HttpHeaders;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SpotifyAuthService {

    private final RestTemplate restTemplate;

    @Value("${spotify.client-id}")
    private String clientId;

    @Value("${spotify.client-secret}")
    private String clientSecret;

    @Value("${spotify.token-url}")
    private String tokenUrl;

    private String accessToken; // cache token
    private long expiryTime;

    public String getAccessToken() {
        // Nếu chưa có token hoặc đã hết hạn thì xin lại token mới
        if (accessToken == null || System.currentTimeMillis() >= expiryTime) {
            fetchNewToken();
        }
        return accessToken;
    }

    private void fetchNewToken() {
        String auth = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> request = new HttpEntity<>("grant_type=client_credentials", headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> body = response.getBody();
            accessToken = (String) body.get("access_token");

            // expires_in trả về số giây -> convert sang millis
            int expiresIn = (Integer) body.get("expires_in");
            expiryTime = System.currentTimeMillis() + (expiresIn - 60) * 1000L;
            // trừ đi 60s để refresh sớm hơn tránh token chết
        } else {
            throw new RuntimeException("Failed to get Spotify token: " + response.getStatusCode());
        }
    }
}
