package com.example.rentalTool_BackEnd.user.web.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "Stare hasło jest wymagane")
        @Size(min = 6, max = 256, message = "Hasło musi mieć od 6 do 256 znaków")
        String oldPassword,

        @NotBlank(message = "Nowe hasło jest wymagane")
        @Size(min = 6, max = 256, message = "Hasło musi mieć od 6 do 256 znaków")
        String newPassword
) {
}
