package com.example.rentalTool_BackEnd.user.web.controller;

import com.example.rentalTool_BackEnd.shared.model.HttpResponse;
import com.example.rentalTool_BackEnd.shared.util.TimeUtil;
import com.example.rentalTool_BackEnd.user.model.User;
import com.example.rentalTool_BackEnd.user.security.jwt.service.TokenService;
import com.example.rentalTool_BackEnd.user.service.UserService;
import com.example.rentalTool_BackEnd.user.web.mapper.UserDtoMapper;
import com.example.rentalTool_BackEnd.user.web.requests.UserUpdateRequest;
import com.example.rentalTool_BackEnd.user.web.requests.UserRoleChangeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

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

    @Operation(summary = "Pobierz użytkownika po ID", description = "Zwraca szczegóły użytkownika")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Użytkownik pobrany pomyślnie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "OK",
                                      "statusCode": 200,
                                      "reason": "User data by id request",
                                      "message": "User by id",
                                      "data": {
                                        "user": {
                                          "id": 1,
                                          "email": "user@example.com",
                                          "firstName": "Jan",
                                          "lastName": "Kowalski"
                                        }
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Użytkownik nie znaleziony",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "NOT_FOUND",
                                      "statusCode": 404,
                                      "reason": "User not found",
                                      "message": "User not found by id"
                                    }
                                    """)))
    })
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

    @Operation(summary = "Aktualizuj dane użytkownika", description = "Aktualizuje dane zalogowanego użytkownika")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dane użytkownika zaktualizowane pomyślnie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "OK",
                                      "statusCode": 200,
                                      "reason": "User update request",
                                      "message": "JWT_TOKEN",
                                      "data": {
                                        "user": {
                                          "id": 1,
                                          "email": "updated@example.com",
                                          "firstName": "Jan",
                                          "lastName": "Kowalski"
                                        }
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Błąd walidacji lub email już zajęty",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "BAD_REQUEST",
                                      "statusCode": 400,
                                      "reason": "Invalid argument",
                                      "message": "Email is already taken by another user"
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp - wymagane zalogowanie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "UNAUTHORIZED",
                                      "statusCode": 401,
                                      "reason": "Authorization failed",
                                      "message": "Unauthorized access - authentication required"
                                    }
                                    """)))
    })
    @PutMapping("/me")
    public ResponseEntity<HttpResponse> updateUser(
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
            authenticatedUser.setEmail(newEmail);
            authenticatedUser.setVerifiedAt(null);
        } else {
            authenticatedUser.setEmail(newEmail);
        }

        if (userUpdateRequest.phoneNumber() != null) {
            authenticatedUser.setPhoneNumber(userUpdateRequest.phoneNumber().trim());
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

    @Operation(summary = "Pobierz wszystkich użytkowników", description = "Zwraca stronicowaną listę wszystkich użytkowników z opcjonalnym wyszukiwaniem (tylko admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista użytkowników pobrana pomyślnie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "OK",
                                      "statusCode": 200,
                                      "reason": "Users list request",
                                      "message": "Users retrieved successfully",
                                      "data": {
                                        "users": [
                                          {
                                            "id": 1,
                                            "email": "user@example.com",
                                            "firstName": "Jan",
                                            "lastName": "Kowalski"
                                          }
                                        ],
                                        "totalElements": 100,
                                        "totalPages": 10,
                                        "currentPage": 0,
                                        "size": 10
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp - wymagane zalogowanie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "UNAUTHORIZED",
                                      "statusCode": 401,
                                      "reason": "Authorization failed",
                                      "message": "Unauthorized access - authentication required"
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień - wymagana rola ADMIN",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "FORBIDDEN",
                                      "statusCode": 403,
                                      "reason": "Access denied",
                                      "message": "Access denied - insufficient permissions"
                                    }
                                    """)))
    })
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

    @Operation(summary = "Zablokuj użytkownika", description = "Blokuje użytkownika uniemożliwiając mu dostęp do systemu (tylko admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Użytkownik zablokowany pomyślnie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "OK",
                                      "statusCode": 200,
                                      "reason": "User blocked",
                                      "message": "User has been blocked successfully",
                                      "data": {
                                        "user": {
                                          "id": 1,
                                          "email": "user@example.com",
                                          "blocked": true
                                        }
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp - wymagane zalogowanie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "UNAUTHORIZED",
                                      "statusCode": 401,
                                      "reason": "Authorization failed",
                                      "message": "Unauthorized access - authentication required"
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień - wymagana rola ADMIN",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "FORBIDDEN",
                                      "statusCode": 403,
                                      "reason": "Access denied",
                                      "message": "Access denied - insufficient permissions"
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Użytkownik nie znaleziony",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "NOT_FOUND",
                                      "statusCode": 404,
                                      "reason": "User not found",
                                      "message": "User not found by id"
                                    }
                                    """)))
    })
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

    @Operation(summary = "Odblokuj użytkownika", description = "Odblokowuje użytkownika przywracając mu dostęp do systemu (tylko admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Użytkownik odblokowany pomyślnie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "OK",
                                      "statusCode": 200,
                                      "reason": "User unblocked",
                                      "message": "User has been unblocked successfully",
                                      "data": {
                                        "user": {
                                          "id": 1,
                                          "email": "user@example.com",
                                          "blocked": false
                                        }
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp - wymagane zalogowanie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "UNAUTHORIZED",
                                      "statusCode": 401,
                                      "reason": "Authorization failed",
                                      "message": "Unauthorized access - authentication required"
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień - wymagana rola ADMIN",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "FORBIDDEN",
                                      "statusCode": 403,
                                      "reason": "Access denied",
                                      "message": "Access denied - insufficient permissions"
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Użytkownik nie znaleziony",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "NOT_FOUND",
                                      "statusCode": 404,
                                      "reason": "User not found",
                                      "message": "User not found by id"
                                    }
                                    """)))
    })
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

    @Operation(summary = "Zmień rolę użytkownika", description = "Zmienia rolę użytkownika (USER, MODERATOR, ADMIN) - tylko admin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rola użytkownika zmieniona pomyślnie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "OK",
                                      "statusCode": 200,
                                      "reason": "User role changed",
                                      "message": "User role has been updated successfully",
                                      "data": {
                                        "user": {
                                          "id": 1,
                                          "email": "user@example.com",
                                          "role": "MODERATOR"
                                        }
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Błąd walidacji - nieprawidłowa rola",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "BAD_REQUEST",
                                      "statusCode": 400,
                                      "reason": "Validation failed",
                                      "message": "Invalid role specified"
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp - wymagane zalogowanie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "UNAUTHORIZED",
                                      "statusCode": 401,
                                      "reason": "Authorization failed",
                                      "message": "Unauthorized access - authentication required"
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień - wymagana rola ADMIN",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "FORBIDDEN",
                                      "statusCode": 403,
                                      "reason": "Access denied",
                                      "message": "Access denied - insufficient permissions"
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Użytkownik nie znaleziony",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "NOT_FOUND",
                                      "statusCode": 404,
                                      "reason": "User not found",
                                      "message": "User not found by id"
                                    }
                                    """)))
    })
    @PatchMapping("/admin/{id}/role")
    public ResponseEntity<HttpResponse> changeUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UserRoleChangeRequest request) {

        User user = userService.changeUserRole(id, request.role());

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
