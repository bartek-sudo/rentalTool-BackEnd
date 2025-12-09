package com.example.rentalTool_BackEnd.user.web.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserRoleChangeRequest(
        @NotBlank(message = "Rola nie może być pusta")
        @Pattern(regexp = "^(USER|MODERATOR|ADMIN)$", 
                 message = "Rola musi być jedną z: USER, MODERATOR, ADMIN")
        String role
) {
}

