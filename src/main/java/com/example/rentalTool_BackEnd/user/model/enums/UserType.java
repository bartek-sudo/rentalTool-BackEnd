package com.example.rentalTool_BackEnd.user.model.enums;

import lombok.Getter;

import java.util.List;

@Getter
public enum UserType {

    USER(List.of("USER")),
    ADMIN(List.of("USER", "ADMIN"));

    private final List<String> authorities;

    UserType(List<String> authorities) {
        this.authorities = authorities;
    }
}
