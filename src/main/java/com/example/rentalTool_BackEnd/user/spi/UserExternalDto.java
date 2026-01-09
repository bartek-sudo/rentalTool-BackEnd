package com.example.rentalTool_BackEnd.user.spi;

public record UserExternalDto(
        long id,
        String email,
        String firstName,
        String lastName,
        String phoneNumber
) {
}
