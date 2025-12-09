package com.example.rentalTool_BackEnd.user.web.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserLoginRequest(
        @Email(message = "Email musi być poprawnie sformatowanym adresem e-mail")
        @NotBlank(message = "Email nie może być pusty")
        String email,
        @NotBlank(message = "Hasło nie może być puste")
        @Size(min = 6, max = 256, message = "Hasło musi mieć od 6 do 256 znaków")
        String password
) {
}
