package com.freedoc.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "{validation.account.notBlank}")
    private String account;

    @NotBlank(message = "{validation.password.notBlank}")
    private String password;

}
