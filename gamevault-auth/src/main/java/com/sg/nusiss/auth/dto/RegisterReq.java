package com.sg.nusiss.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterReq {
    @Email @NotBlank public String email;
    @NotBlank public String username;
    @Size(min = 6) public String password;
}
