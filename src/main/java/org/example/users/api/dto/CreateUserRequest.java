package org.example.users.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserRequest {

    @NotBlank
    private String username;

    private String surname;

    @NotBlank
    private String name;

    private Boolean isPremium;

    @NotBlank
    private String password;
}
