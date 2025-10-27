package com.HuyHoang.DTO.request;

import com.HuyHoang.Entity.FavoriteSongs;
import com.HuyHoang.Entity.ListeningHistory;
import com.HuyHoang.Entity.Playlist;
import com.HuyHoang.validator.DobConstraint;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {



    @Size(min =3, message = "USERNAME_INVALID")
    String username;
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
