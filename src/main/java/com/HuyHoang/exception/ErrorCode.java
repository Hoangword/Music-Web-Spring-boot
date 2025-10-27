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
    INVALID_TOKEN(1014," Invalid Token", HttpStatus.BAD_REQUEST)
    ;
    int code;
    String message;
    HttpStatusCode httpStatusCode;
}
