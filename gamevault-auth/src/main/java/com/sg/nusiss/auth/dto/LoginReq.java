package com.sg.nusiss.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginReq {
    @NotBlank public String username;
    @NotBlank public String password;
}
