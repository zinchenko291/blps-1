package me.zinch.itmo.mts.web.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank
    private String login;

    @NotBlank
    private String password;
}
