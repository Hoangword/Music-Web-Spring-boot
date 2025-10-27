package com.HuyHoang.service;

import com.HuyHoang.DTO.JwtInfo;
import com.HuyHoang.DTO.TokenPayload;
import com.HuyHoang.DTO.request.*;
import com.HuyHoang.DTO.response.AuthenticationResponse;
import com.HuyHoang.DTO.response.IntrospectResponse;
import com.HuyHoang.DTO.response.LoginResponse;
import com.HuyHoang.Entity.InvalidedToken;
import com.HuyHoang.Entity.RedisToken;
import com.HuyHoang.Entity.User;
import com.HuyHoang.exception.AppException;
import com.HuyHoang.exception.ErrorCode;
import com.HuyHoang.repository.InvalidedTokenRepository;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    InvalidedTokenRepository invalidedTokenRepository;


    private final JwtService jwtService;
    // PzVoW4QmWUOmIkLkS3eZwOshHeXG3bQcRh38eXw3qjWxgdCBOFVxcO+xlCoaoQha
    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

//    private final  AuthenticationManager authenticationManager;
//
//    private final RedisTokenRepository redisTokenRepository;
//    public LoginResponse login(LoginRequest request){
//        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(request.getUsername(),request.getPassword());
//        Authentication authenticate = authenticationManager.authenticate(authenticationToken);
//
//        User user = (User) authenticate.getPrincipal();
//        TokenPayload accessPayload = jwtService.generateAccessToken(user);
//        TokenPayload refreshPayload = jwtService.generateRefreshToken(user);
//
//        redisTokenRepository.save(
//            RedisToken.builder()
//                    .jwtId(refreshPayload.getJwtId())
//                    .expiredTime(refreshPayload.getExpiredTime().getTime())
//                    .build()
//        );
//
//        return LoginResponse.builder()
//                .accessToken(accessPayload.getToken())
//                .refreshToken(refreshPayload.getToken())
//                .build();
//    }

    public IntrospectResponse introspect(IntrospectRequest request)
            throws JOSEException, ParseException {
        var token = request.getToken();
        boolean checkValid = true;

        try{
            verifyToken(token,false);
        }catch(AppException e){
            checkValid = false;
        }


        return IntrospectResponse.builder()
                .valid(checkValid)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request){
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if(!authenticated){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        var token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        var signedJWT = verifyToken(request.getToken(),true);

        String jit = signedJWT.getJWTClaimsSet().getJWTID();
        Date expireTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        invalidedTokenRepository.save(InvalidedToken.builder().id(jit).expiryTime(expireTime).build());

        var user = userRepository.findByUsername(signedJWT.getJWTClaimsSet().getSubject())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        var token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();

    }
    public String generateToken(User user){
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("HuyHoang.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                                Instant.now().plus(VALID_DURATION, ChronoUnit.MINUTES).toEpochMilli()
                        )
                )
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject( jwsHeader, payload);

        try {
            jwsObject.sign( new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }



    public void logout(LogoutRequest request) throws ParseException, JOSEException {

        try{
            var signToken = verifyToken(request.getToken(),true);

            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expireTime = signToken.getJWTClaimsSet().getExpirationTime();

            invalidedTokenRepository.save(InvalidedToken.builder()
                    .id(jit)
                    .expiryTime(expireTime)
                    .build());
        }catch (AppException e){
            log.info("token already expired");
        }
    }
//
//    public void logout(String token) throws ParseException {
//        JwtInfo jwtInfo =  jwtService.parseToken(token);
//        if(jwtInfo.getExpiredTime().before(new Date())){
//            return;
//        }
//        Date now = new Date();
//        RedisToken redisToken = RedisToken.builder()
//                .jwtId(jwtInfo.getJwtId())
//                .expiredTime(jwtInfo.getExpiredTime().getTime() - now.getTime())
//                .build();
//        redisTokenRepository.save(redisToken);
//        log.info("Logout success");
//    }

    private SignedJWT verifyToken(String token, boolean checkRefresh) throws JOSEException, ParseException {
            JWSVerifier jwsVerifier = new MACVerifier(SIGNER_KEY.getBytes());
            SignedJWT signedJWT = SignedJWT.parse(token);

            Date expireTime = (checkRefresh)
                    ? new Date(signedJWT.getJWTClaimsSet().getExpirationTime()
                    .toInstant().plus(REFRESHABLE_DURATION,ChronoUnit.MINUTES).toEpochMilli())
                        : signedJWT.getJWTClaimsSet().getExpirationTime();

            var verified = signedJWT.verify(jwsVerifier);

            if(!(verified && expireTime.after(new Date())))
                throw new AppException(ErrorCode.UNAUTHENTICATED);

            if(invalidedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
                throw new AppException(ErrorCode.UNAUTHENTICATED);

            return signedJWT;
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

}
