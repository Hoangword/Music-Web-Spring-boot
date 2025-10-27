package com.HuyHoang.DTO.request;

import com.HuyHoang.validator.DobConstraint;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
     String password;
     String firstName;
     String lastName;

     @DobConstraint(min = 16 , message = "INVALID_DOB")
     LocalDate dob;
     List<String> roles;
}
