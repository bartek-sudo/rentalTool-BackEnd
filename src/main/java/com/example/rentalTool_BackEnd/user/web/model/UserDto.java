package com.example.rentalTool_BackEnd.user.web.model;

public record UserDto(
        long id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        boolean verified,
        boolean blocked,
        String createdAt,
        String updatedAt,
        String blockedAt,
        String verifiedAt,
        String userType

) {
}
