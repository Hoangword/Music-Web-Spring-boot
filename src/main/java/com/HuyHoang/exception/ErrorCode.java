package com.HuyHoang.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error",HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Invalid message key",HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed",HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters",HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters",HttpStatus.BAD_REQUEST),
    INVALID_FIRSTNAME(1005, "first Name have to at least {min} characters",HttpStatus.BAD_REQUEST),
    INVALID_LASTNAME(1006, "last Name have to at least {min} characters",HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1007, "User not existed",HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1008, "Unauthenticated",HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1009, "You do not have permission",HttpStatus.FORBIDDEN),
    PLAYLIST_EXISTED(1010, "Playlist have been existed ",HttpStatus.BAD_REQUEST),
    PLAYLIST_NOT_EXISTED(1011, "Playlist have not been existed ",HttpStatus.NOT_FOUND),
    INVALID_DOB(1012, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    ROLE_NOT_EXISTED(1013," Role not existed", HttpStatus.NOT_FOUND),
    INVALID_TOKEN(1014," Invalid Token", HttpStatus.BAD_REQUEST),
    INVALID_REFRESH_TOKEN(1015, "Invalid refresh Token", HttpStatus.BAD_REQUEST),
    REFRESH_TOKEN_NOT_EXISTED(1016, "refresh Token not found", HttpStatus.NOT_FOUND),
    REDIS_TOKEN_NOT_EXISTED(1017, "Redis Token not found", HttpStatus.NOT_FOUND),
    TOKEN_ALREADY_EXPIRED(1018, "Token already expired for user",HttpStatus.BAD_REQUEST ),
    OTP_OR_USER_INVALID(1019, "wrong otp or user ",HttpStatus.NOT_FOUND),
    OTP_ALREADY_EXPIRED(1020,"Otp has expired",HttpStatus.BAD_REQUEST),
    PASSWORD_INCORRECT(1021, "Please enter the password again!",HttpStatus.EXPECTATION_FAILED),
    EMAIL_INVALID(1022, "email not existed",HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_EXISTED(1023, "email has been used" , HttpStatus.BAD_REQUEST),
    EMAIL_UNVERIFIED(1024, "Email has not been verified. Please check your email to verify your account ",HttpStatus.EXPECTATION_FAILED)
    ;
    int code;
    String message;
    HttpStatusCode httpStatusCode;
}
