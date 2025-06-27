package com.example.rentalTool_BackEnd.user.model;

import com.example.rentalTool_BackEnd.user.model.enums.UserType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String firstName;

    private String lastName;

    private String email;

    private String password;

    @Column(nullable = false)
    private boolean blocked;

    private boolean verified;

    private Instant createdAt;

    private Instant updatedAt;

    private Instant changedPasswordAt;

    private Instant blockedAt;

    private Instant verifiedAt;

    @Enumerated(EnumType.STRING)
    private UserType userType;

    public User(String password, String email, String lastName, String firstName, UserType userType) {
        this.password = password;
        this.email = email;
        this.lastName = lastName;
        this.firstName = firstName;
        this.userType = userType;

        // TODO add verification system
        this.verified = true;
        this.blocked = false;

        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.changedPasswordAt = Instant.now();
        this.blockedAt = Instant.now();
        this.verifiedAt = Instant.now();

    }

    public void changePassword(String password) {
        this.password = password;
        this.changedPasswordAt = Instant.now();
        this.updatedAt = Instant.now();
    }
}
