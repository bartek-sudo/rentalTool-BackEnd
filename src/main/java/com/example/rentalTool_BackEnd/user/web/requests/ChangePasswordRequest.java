package com.example.rentalTool_BackEnd.user.web.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank
        @Size(min = 6,max = 256)
        String oldPassword,
        @NotBlank
        @Size(min = 6,max = 256)
        String newPassword
) {
}
