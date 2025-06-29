package com.example.rentalTool_BackEnd.user.model.enums;

import lombok.Getter;

import java.util.List;

@Getter
public enum UserType {

    USER(List.of("USER")),
    MODERATOR(List.of("USER", "MODERATOR")),
    ADMIN(List.of("USER", "MODERATOR", "ADMIN"));

    private final List<String> authorities;

    UserType(List<String> authorities) {
        this.authorities = authorities;
    }
}
