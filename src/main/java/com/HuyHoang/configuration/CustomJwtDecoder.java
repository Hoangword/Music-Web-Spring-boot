package com.HuyHoang.configuration;

import com.HuyHoang.DTO.request.IntrospectRequest;
import com.HuyHoang.exception.AppException;
import com.HuyHoang.exception.ErrorCode;
import com.HuyHoang.service.AuthenticationService;
import com.HuyHoang.service.JwtService;
import com.nimbusds.jose.JOSEException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.text.ParseException;
import java.util.Objects;

@Component
public class CustomJwtDecoder implements JwtDecoder {

    @Value("${jwt.signerKey}")
    private String signerKey;

    @Autowired
    private  AuthenticationService authenticationService;


   // private final JwtService jwtService;

    private NimbusJwtDecoder nimbusJwtDecoder = null;

    @Override
    public Jwt decode(String token) throws JwtException {
        try{
            var response = authenticationService.introspect(
                    IntrospectRequest.builder()
                            .token(token)
                            .build()
            );
            if(!response.isValid())
                throw new JwtException("Token invalid");
        } catch (ParseException | JOSEException e) {
            throw new JwtException(e.getMessage());
        }

        if(Objects.isNull(nimbusJwtDecoder)){
            SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(),"HS512");

            nimbusJwtDecoder= NimbusJwtDecoder.withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS512)
                    .build();
        }
        return nimbusJwtDecoder.decode(token);
    }

//    @Override
//    public Jwt decode(String token) throws JwtException {
//        try {
//          if(!jwtService.verifyJwtToken(token)){
//              throw new AppException(ErrorCode.UNAUTHENTICATED);
//          }
//
//
//        } catch (JOSEException | ParseException e) {
//            throw new JwtException(e.getMessage());
//        }
//
//        if(Objects.isNull(nimbusJwtDecoder)){
//            SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(),"HS512");
//
//            nimbusJwtDecoder= NimbusJwtDecoder.withSecretKey(secretKeySpec)
//                    .macAlgorithm(MacAlgorithm.HS512)
//                    .build();
//        }
//        return nimbusJwtDecoder.decode(token);
//    }
}
