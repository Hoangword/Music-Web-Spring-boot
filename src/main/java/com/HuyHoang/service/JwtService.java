package com.HuyHoang.service;

import com.HuyHoang.DTO.JwtInfo;
import com.HuyHoang.DTO.TokenPayload;
import com.HuyHoang.DTO.response.TokenVerificationResponse;
import com.HuyHoang.Entity.RedisToken;
import com.HuyHoang.Entity.User;
import com.HuyHoang.exception.AppException;
import com.HuyHoang.exception.ErrorCode;
import com.HuyHoang.repository.RedisTokenRepository;
import com.HuyHoang.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;


@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtService {
    private final UserRepository userRepository;
    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    RedisTemplate<String, Object> redisTemplate;
//    private final RedisTokenRepository redisTokenRepository;

    public TokenPayload generateAccessToken(User user){
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

        Date issueTime = new Date();
        Date expireTime = new Date(
                Instant.now().plus(1200, ChronoUnit.SECONDS).toEpochMilli()
        );
        String jwtId = UUID.randomUUID().toString();

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("HuyHoang.com")
                .issueTime(issueTime)
                .expirationTime(expireTime)
                .jwtID(jwtId)
                .claim("scope", buildScope(user))
                .claim("type", "access")
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject( jwsHeader, payload);

        try {
            jwsObject.sign( new MACSigner(SIGNER_KEY.getBytes()));
            String token = jwsObject.serialize();
            return TokenPayload.builder()
                    .token(token)
                    .jwtId(jwtId)
                    .expiredTime(expireTime)
                    .build();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    public TokenPayload generateRefreshToken(User user){
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

        Date issueTime = new Date();
        Date expireTime = new Date(
                Instant.now().plus(3600, ChronoUnit.SECONDS).toEpochMilli()
        );
        String jwtId = UUID.randomUUID().toString();

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("HuyHoang.com")
                .issueTime(issueTime)
                .expirationTime(expireTime)
                .jwtID(jwtId)
                .claim("scope", buildScope(user))
                .claim("type", "refresh")
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject( jwsHeader, payload);

        try {
            jwsObject.sign( new MACSigner(SIGNER_KEY.getBytes()));
            String token = jwsObject.serialize();
            return TokenPayload.builder()
                    .token(token)
                    .jwtId(jwtId)
                    .expiredTime(expireTime)
                    .build();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    public TokenVerificationResponse verifyJwtToken(String token) throws JOSEException, ParseException {
        try {
            JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());


            SignedJWT signedJWT = SignedJWT.parse(token);
            String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
            Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            String username = signedJWT.getJWTClaimsSet().getSubject();

            var verified = signedJWT.verify(verifier);
            JWTClaimsSet jwtClaimsSet = signedJWT.getJWTClaimsSet();

            if (!(verified) || expiryTime.before(new Date())){
                log.info("token het han");
                return TokenVerificationResponse.builder()
                        .isValid(false)
                        .errorMessage(ErrorCode.TOKEN_ALREADY_EXPIRED.getMessage())
                        .build();

            }

            RedisToken redisToken = null;

            if(signedJWT.getJWTClaimsSet().getStringClaim("type").equals("refresh")){
                String accessTokenId = (String) redisTemplate.opsForValue()
                        .get("refreshToken:" + jwtId);

                if (accessTokenId == null) {
                    log.warn("Refresh token không tồn tại trong Redis");
                    throw new AppException(ErrorCode.TOKEN_ALREADY_EXPIRED);
                }

                redisToken = (RedisToken) redisTemplate.opsForValue()
                        .get("accessToken:" + accessTokenId);

            }else{
                redisToken = (RedisToken) redisTemplate.opsForValue()
                        .get("accessToken:" + jwtId);

            }

            if(!redisToken.getAccessTokenId().equals(jwtId) && !redisToken.getRefreshTokenId().equals(jwtId) ){
                log.info("token khong co trong redis");
                throw new AppException(ErrorCode.INVALID_TOKEN);
            }


            String scope = jwtClaimsSet.getStringClaim("scope");
            Set<String> permissions = new HashSet<>();
            if (scope != null && !scope.isEmpty()) {
                permissions = new HashSet<>(Arrays.asList(scope.split(" ")));
            }


            return TokenVerificationResponse.builder()
                    .isValid(true)
                    .jwtId(jwtClaimsSet.getJWTID())
                    .username(jwtClaimsSet.getSubject())
                    .permissions(permissions)
                    .build();
        }catch (Exception e){
            return TokenVerificationResponse.builder().isValid(false)
                    .errorMessage("Token verification exception: " + e.getMessage())
                    .build();

        }
    }



    private String buildScope(User user){
        StringJoiner stringJoiner = new StringJoiner(" ");
        if(!CollectionUtils.isEmpty(user.getRoles()))
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_"+role.getName());
                if(!CollectionUtils.isEmpty(role.getPermissions()))
                    role.getPermissions()
                            .forEach(permission -> stringJoiner.add(permission.getName()));
            });

        return stringJoiner.toString();
    }

    public JwtInfo parseToken(String token) throws ParseException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
        Date issueTime = signedJWT.getJWTClaimsSet().getIssueTime();
        Date expireTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        String subject = signedJWT.getJWTClaimsSet().getSubject();
        return JwtInfo.builder()
                .jwtId(jwtId)
                .username(subject)
                .issueTime(issueTime)
                .expiredTime(expireTime)
                .build();

    }
}
