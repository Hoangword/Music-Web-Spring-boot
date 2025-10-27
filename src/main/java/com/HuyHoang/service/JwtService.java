package com.HuyHoang.service;

import com.HuyHoang.DTO.JwtInfo;
import com.HuyHoang.DTO.TokenPayload;
import com.HuyHoang.Entity.RedisToken;
import com.HuyHoang.Entity.User;
import com.HuyHoang.exception.AppException;
import com.HuyHoang.exception.ErrorCode;
import com.HuyHoang.repository.RedisTokenRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtService {
    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    private final RedisTokenRepository redisTokenRepository;

    public TokenPayload generateAccessToken(User user){
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

        Date issueTime = new Date();
        Date expireTime = new Date(
                Instant.now().plus(VALID_DURATION, ChronoUnit.MINUTES).toEpochMilli()
        );
        String jwtId = UUID.randomUUID().toString();

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("HuyHoang.com")
                .issueTime(issueTime)
                .expirationTime(expireTime)
                .jwtID(jwtId)
                .claim("scope", buildScope(user))
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
                Instant.now().plus(14, ChronoUnit.DAYS).toEpochMilli()
        );
        String jwtId = UUID.randomUUID().toString();

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("HuyHoang.com")
                .issueTime(issueTime)
                .expirationTime(expireTime)
                .jwtID(jwtId)
                .claim("scope", buildScope(user))
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

    public boolean verifyJwtToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

//        if (!(verified && expiryTime.after(new Date())))
//            throw new AppException(ErrorCode.UNAUTHENTICATED);

        if(expiryTime.before(new Date())){
            return false;
        }

        String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
        Optional<RedisToken> byId = redisTokenRepository.findById(jwtId);
        if(byId.isPresent()){
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        return verified ;
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

        return JwtInfo.builder()
                .jwtId(jwtId)
                .issueTime(issueTime)
                .expiredTime(expireTime)
                .build();

    }
}
