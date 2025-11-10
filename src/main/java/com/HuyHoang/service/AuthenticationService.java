package com.HuyHoang.service;

import com.HuyHoang.DTO.JwtInfo;
import com.HuyHoang.DTO.TokenPayload;
import com.HuyHoang.DTO.request.*;
import com.HuyHoang.DTO.response.*;
import com.HuyHoang.Entity.InvalidedToken;
import com.HuyHoang.Entity.RedisToken;
import com.HuyHoang.Entity.Role;
import com.HuyHoang.Entity.User;
import com.HuyHoang.exception.AppException;
import com.HuyHoang.exception.ErrorCode;
import com.HuyHoang.mapper.UserMapper;
import com.HuyHoang.repository.InvalidedTokenRepository;
import com.HuyHoang.repository.RedisTokenRepository;
import com.HuyHoang.repository.RoleRepository;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.security.SecureRandom;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    InvalidedTokenRepository invalidedTokenRepository;
    RedisTemplate<String, Object> redisTemplate;

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

    private final int TOKEN_EXPIRY_MINUTES = 10;

    UserMapper userMapper;

    PasswordEncoder passwordEncoder;
    private final  AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;
    private final EmailService emailService;

//    private final RedisTokenRepository redisTokenRepository;
    public LoginResponse login(LoginRequest request){
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(request.getUsername(),request.getPassword());
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);

        User user = (User) authenticate.getPrincipal();
        TokenPayload accessPayload = jwtService.generateAccessToken(user);
        TokenPayload refreshPayload = jwtService.generateRefreshToken(user);


        if (!user.isEmailVerified()) {
            throw new AppException(ErrorCode.EMAIL_UNVERIFIED);
        }

        long accessTtlSeconds =
                (accessPayload.getExpiredTime().getTime() - System.currentTimeMillis()) / 1000;

        long refreshTtlSeconds =
                (refreshPayload.getExpiredTime().getTime() - System.currentTimeMillis()) / 1000;

        RedisToken redisToken =RedisToken.builder()
                .userId(user.getId())
                .accessTokenId(accessPayload.getJwtId())
                .refreshTokenId(refreshPayload.getJwtId())
                .expiredTime(refreshTtlSeconds)
                .build();

        redisTemplate.opsForValue().set(
                "accessToken:" + redisToken.getAccessTokenId(),
                redisToken,
                accessTtlSeconds,
                TimeUnit.SECONDS
        );


        redisTemplate.opsForValue().set(
                "refreshToken:" + redisToken.getRefreshTokenId(),
                redisToken.getAccessTokenId(),
                refreshTtlSeconds,
                TimeUnit.SECONDS
        );

        return LoginResponse.builder()
                .accessToken(accessPayload.getToken())
                .refreshToken(refreshPayload.getToken())
                .build();
    }



    public String registerWithEmailVerify(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if(userRepository.findByEmail(request.getEmail()).isPresent()){
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTED);
        }

        // Tạo user chưa kích hoạt
        User user = userMapper.toUser(request);

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Role role = roleRepository.findByName("USER")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
        Set<Role> roles = new HashSet<>();
        roles.add(role);

        user.setRoles(roles);

        // Sinh mã xác thực email (OTP)
        String otp = generateEmailVerificationToken();
        String hashedOtp = passwordEncoder.encode(otp);

        user.setEmailVerificationToken(hashedOtp);
        user.setEmailVerificationTokenExpiryDate(LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES));

        userRepository.save(user);

        // Gửi email xác thực
        String subject = "Verify your email address";
        String body = String.format("""
                Chào %s,

                Đây là mã xác thực tài khoản của bạn: %s
                Mã này sẽ hết hạn sau %d phút.

                Cảm ơn bạn đã đăng ký!
                """, request.getUsername(), otp, TOKEN_EXPIRY_MINUTES);

        try {
            emailService.sendEmail(request.getEmail(), subject, body);
        } catch (Exception e) {
            log.error("Error while sending email: {}", e.getMessage());
        }

        return ("User registered. Please verify your email.");
    }

    private String generateEmailVerificationToken() {
        SecureRandom random = new SecureRandom();
        StringBuilder token = new StringBuilder(5);
        for (int i = 0; i < 5; i++) {
            token.append(random.nextInt(10));
        }
        return token.toString();
    }

    public void verifyRegisterEmail(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        if (user.getEmailVerificationTokenExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification code has expired");
        }

        if (passwordEncoder.matches(otp, user.getEmailVerificationToken())) {
            user.setEmailVerified(true);
            user.setEmailVerificationToken(null);
            user.setEmailVerificationTokenExpiryDate(null);
            userRepository.save(user);
        } else {
            throw new RuntimeException("Invalid verification code");
        }
    }


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



//    public void logout(LogoutRequest request) throws ParseException, JOSEException {
//
//        try{
//            var signToken = verifyToken(request.getToken(),true);
//
//            String jit = signToken.getJWTClaimsSet().getJWTID();
//            Date expireTime = signToken.getJWTClaimsSet().getExpirationTime();
//
//            invalidedTokenRepository.save(InvalidedToken.builder()
//                    .id(jit)
//                    .expiryTime(expireTime)
//                    .build());
//        }catch (AppException e){
//            log.info("token already expired");
//        }
//    }
//

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
    public TokenResponse refreshJwtToken(RefreshTokenRequest request) throws ParseException, JOSEException {
        String refreshToken = request.getRefreshToken();

        // 1️⃣ Xác minh refresh token hợp lệ
        TokenVerificationResponse verificationResponse = jwtService.verifyJwtToken(refreshToken);
        if (!verificationResponse.isValid()) {
            log.info("Lỗi refreshtoken không hợp lệ");
            log.info(verificationResponse.getErrorMessage());
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String username = verificationResponse.getUsername();
        String refreshTokenId = verificationResponse.getJwtId();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        try{

        String accessTokenId = (String) redisTemplate.opsForValue()
                .get("refreshToken:" + refreshTokenId);

        RedisToken redisToken = (RedisToken) redisTemplate.opsForValue()
                .get("accessToken:" + accessTokenId);

        redisTemplate.delete("accessToken:" + redisToken.getAccessTokenId());
        redisTemplate.delete("refreshToken:" + redisToken.getRefreshTokenId());


        TokenPayload newAccessToken = jwtService.generateAccessToken(user);
        TokenPayload newRefreshToken = jwtService.generateRefreshToken(user);

        long accessTtlSeconds = (newAccessToken.getExpiredTime().getTime() - System.currentTimeMillis()) / 1000;

        long refreshTtlSeconds = (newRefreshToken.getExpiredTime().getTime() - System.currentTimeMillis()) / 1000;
        redisToken.setAccessTokenId(newAccessToken.getJwtId());
        redisToken.setRefreshTokenId(newRefreshToken.getJwtId());
        redisToken.setUserId(user.getId());
        redisToken.setExpiredTime(refreshTtlSeconds);



        redisTemplate.opsForValue().set(
                    "accessToken:" + redisToken.getAccessTokenId(),
                    redisToken,
                    accessTtlSeconds,
                    TimeUnit.SECONDS
        );

        redisTemplate.opsForValue().set(
                    "refreshToken:" + redisToken.getRefreshTokenId(),
                    redisToken.getAccessTokenId(),
                    refreshTtlSeconds,
                    TimeUnit.SECONDS
        );

            // Trả về response
        return TokenResponse.builder()
                .accessToken(newAccessToken.getToken())
                .refreshToken(newRefreshToken.getToken())
                .build();
        }catch (Exception e){
            throw new AppException(ErrorCode.TOKEN_ALREADY_EXPIRED);
        }
    }


    public void logout(String accessToken) throws ParseException {
       try{

        JwtInfo jwtInfo =  jwtService.parseToken(accessToken);
        if(jwtInfo.getExpiredTime().before(new Date())){
            throw new AppException(ErrorCode.TOKEN_ALREADY_EXPIRED);
        }



           RedisToken redisToken = (RedisToken) redisTemplate.opsForValue()
                   .get("accessToken:" + jwtInfo.getJwtId());

           redisTemplate.delete("accessToken:" + redisToken.getAccessTokenId());
           redisTemplate.delete("refreshToken:" + redisToken.getRefreshTokenId());




        log.info("Logout success");
       }catch(Exception e){
           log.error("Có lỗi trong quá trình logout: {}", e.getMessage());
           log.info("token already expired");
           throw new AppException(ErrorCode.TOKEN_ALREADY_EXPIRED);
       }
    }

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
