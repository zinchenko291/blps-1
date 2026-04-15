package me.zinch.itmo.mts.web.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import me.zinch.itmo.mts.domain.enums.UserRole;

@Getter
@Setter
public class RegisterManagerRequest {

    @NotBlank
    @Size(min = 3, max = 100)
    private String login;

    @NotBlank
    @Size(min = 6, max = 255)
    private String password;

    @NotBlank
    @Size(max = 150)
    private String name;

    @NotNull
    private UserRole role;
}
