package com.example.rentalTool_BackEnd.user.web.model;

public record UserDto(
        String firstName,
        String lastName,
        String email,
        boolean verified,
        boolean blocked,
        String createdAt,
        String updatedAt,
        String blockedAt,
        String verifiedAt
) {
}
