package com.kukuxer.registration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import jakarta.validation.constraints.NotNull;
@Data
public class UserDTO {

    private Long id;
    @NotNull(message = "name must be not mull")
    @Length(max = 255, message = "Name length must be shorter than 255 symbols")
    private String username;

    @NotNull(message = "username must be not mull")
    @Length(max = 255, message = "Username length must be shorter than 255 symbols")
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotNull(message = "Password must be not null.")
    private String password;
}
