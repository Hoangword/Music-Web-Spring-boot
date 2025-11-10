package com.HuyHoang.DTO.request;

import com.HuyHoang.validator.DobConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterRequest {

    @Size(min =3, message = "USERNAME_INVALID")
    @NotBlank(message = "Username can not be blank")
    String username;

    @NotBlank(message = "Password can not be blank")
    @Size(min =8, message = "INVALID_PASSWORD")
    String password;

    @Size(min =2, message = "INVALID_FIRSTNAME")
    String firstName;


    @Size(min =2, message = "INVALID_LASTNAME")
    String lastName;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email format is invalid")
    String email;

    @DobConstraint(min = 16 , message = "INVALID_DOB")
    LocalDate dob;
}
