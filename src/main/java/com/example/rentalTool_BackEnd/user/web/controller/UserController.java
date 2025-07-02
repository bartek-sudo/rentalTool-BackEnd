package com.example.rentalTool_BackEnd.user.web.controller;

import com.example.rentalTool_BackEnd.shared.model.HttpResponse;
import com.example.rentalTool_BackEnd.shared.util.TimeUtil;
import com.example.rentalTool_BackEnd.user.exception.UserNotFoundException;
import com.example.rentalTool_BackEnd.user.model.User;
import com.example.rentalTool_BackEnd.user.security.jwt.service.TokenService;
import com.example.rentalTool_BackEnd.user.service.UserService;
import com.example.rentalTool_BackEnd.user.web.mapper.UserDtoMapper;
import com.example.rentalTool_BackEnd.user.web.requests.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserDtoMapper userDtoMapper;
    private final TokenService tokenService;

    @GetMapping("/{id}")
    public ResponseEntity<HttpResponse> getUserById(@PathVariable("id") long id) {
        return ResponseEntity.status(OK)
                .body(HttpResponse.builder()
                        .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                        .statusCode(OK.value())
                        .httpStatus(OK)
                        .reason("User data by id request")
                        .message("User by id")
                        .data(Map.of("user", userDtoMapper.toDto(userService.getUserById(id))))
                        .build());
    }

    @PutMapping("/me")
    public ResponseEntity<HttpResponse> updateUser(
//            @PathVariable("id") long id,
            @RequestBody UserUpdateRequest userUpdateRequest,
            Authentication authentication) {

        User authenticatedUser = userService.getUserByEmail(authentication.getName());

        if (userUpdateRequest.firstName() != null && !authenticatedUser.getFirstName().trim().isEmpty()) {
            authenticatedUser.setFirstName(userUpdateRequest.firstName().trim());
        }

        if (userUpdateRequest.lastName() != null && !authenticatedUser.getLastName().trim().isEmpty()) {
            authenticatedUser.setLastName(userUpdateRequest.lastName().trim());
        }

        String oldEmail = authenticatedUser.getEmail();
        String newEmail = userUpdateRequest.email().trim();

        Optional<User> existingUser = userService.findOptionalByEmail(newEmail);

        if (existingUser.isPresent() && existingUser.get().getId() != authenticatedUser.getId()) {
            throw new IllegalArgumentException("Email is already taken by another user");
        }

        if (!oldEmail.equalsIgnoreCase(newEmail)) {
            // Email się zmienił – cofamy weryfikację
            authenticatedUser.setEmail(newEmail);
//            authenticatedUser.setVerified(false); //todo
            authenticatedUser.setVerifiedAt(null);
            // TODO: Wyślij maila weryfikacyjnego na nowy adres
        } else {
            authenticatedUser.setEmail(newEmail);
        }

        authenticatedUser.setUpdatedAt(Instant.now());

        User updatedUser = userService.updateUser(authenticatedUser);

        final Authentication newAuth = new UsernamePasswordAuthenticationToken(
                updatedUser.getEmail(), authentication.getCredentials(), authentication.getAuthorities());

        final String newToken = tokenService.generateJwtToken(newAuth, updatedUser);

        return ResponseEntity.status(OK)
                .body(HttpResponse.builder()
                        .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                        .statusCode(OK.value())
                        .httpStatus(OK)
                        .reason("User update request")
                        .message(newToken)
                        .data(Map.of("user", userDtoMapper.toDto(updatedUser)))
                        .build());
    }

    @GetMapping("/admin")
    public ResponseEntity<HttpResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userService.getAllUsers(pageable, search);

        return ResponseEntity.status(OK)
                .body(HttpResponse.builder()
                        .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                        .statusCode(OK.value())
                        .httpStatus(OK)
                        .reason("Users list request")
                        .message("Users retrieved successfully")
                        .data(Map.of(
                                "users", users.getContent().stream()
                                        .map(userDtoMapper::toDto)
                                        .toList(),
                                "totalElements", users.getTotalElements(),
                                "totalPages", users.getTotalPages(),
                                "currentPage", users.getNumber(),
                                "size", users.getSize()
                        ))
                        .build());
    }

    @PatchMapping("/admin/{id}/block")
    public ResponseEntity<HttpResponse> blockUser(@PathVariable Long id) {
        User user = userService.blockUser(id);
        return ResponseEntity.status(OK)
                .body(HttpResponse.builder()
                        .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                        .statusCode(OK.value())
                        .httpStatus(OK)
                        .reason("User blocked")
                        .message("User has been blocked successfully")
                        .data(Map.of("user", userDtoMapper.toDto(user)))
                        .build());
    }

    @PatchMapping("/admin/{id}/unblock")
    public ResponseEntity<HttpResponse> unblockUser(@PathVariable Long id) {
        User user = userService.unblockUser(id);
        return ResponseEntity.status(OK)
                .body(HttpResponse.builder()
                        .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                        .statusCode(OK.value())
                        .httpStatus(OK)
                        .reason("User unblocked")
                        .message("User has been unblocked successfully")
                        .data(Map.of("user", userDtoMapper.toDto(user)))
                        .build());
    }

    @PatchMapping("/admin/{id}/role")
    public ResponseEntity<HttpResponse> changeUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String role = body.get("role");
        User user = userService.changeUserRole(id, role);

        return ResponseEntity.status(OK)
                .body(HttpResponse.builder()
                        .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                        .statusCode(OK.value())
                        .httpStatus(OK)
                        .reason("User role changed")
                        .message("User role has been updated successfully")
                        .data(Map.of("user", userDtoMapper.toDto(user)))
                        .build());
    }



}
