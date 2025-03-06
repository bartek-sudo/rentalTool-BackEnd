package com.example.rentalTool_BackEnd.user.model;

import com.example.rentalTool_BackEnd.user.model.enums.UserType;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String firstName;

    private String lastName;

    private String email;

    private String password;

    private boolean blocked;

    private boolean verified;

    private Instant createdAt;

    private Instant updatedAt;

    private Instant changedPasswordAt;

    private Instant blockedAt;

    private Instant verifiedAt;

    @Enumerated(EnumType.STRING)
    private UserType userType;
}
