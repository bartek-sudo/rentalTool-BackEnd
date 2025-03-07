package com.example.rentalTool_BackEnd.user.web.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegisterRequest(
        @Email
        @NotBlank
        String email,
        @NotBlank
        @Size(min = 6, max = 256)
        String password,
        @NotBlank
        String firstName,
        @NotBlank
        String lastName
) {
}
