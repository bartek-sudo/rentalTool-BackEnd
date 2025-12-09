package com.example.rentalTool_BackEnd.user.web.controller;

import com.example.rentalTool_BackEnd.shared.model.HttpResponse;
import com.example.rentalTool_BackEnd.shared.util.TimeUtil;
import com.example.rentalTool_BackEnd.user.exception.IllegalAccountAccessException;
import com.example.rentalTool_BackEnd.user.model.User;
import com.example.rentalTool_BackEnd.user.security.exception.EmailNotVerifiedException;
import com.example.rentalTool_BackEnd.user.model.enums.UserType;
import com.example.rentalTool_BackEnd.user.security.jwt.service.TokenService;
import com.example.rentalTool_BackEnd.user.security.provider.AccountAuthenticationProvider;
import com.example.rentalTool_BackEnd.user.service.EmailVerificationService;
import com.example.rentalTool_BackEnd.user.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import com.example.rentalTool_BackEnd.user.web.mapper.UserDtoMapper;
import com.example.rentalTool_BackEnd.user.web.requests.ChangePasswordRequest;
import com.example.rentalTool_BackEnd.user.web.requests.UserLoginRequest;
import com.example.rentalTool_BackEnd.user.web.requests.UserRegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
class AuthController {

    private final UserService userService;
    private final AccountAuthenticationProvider accountAuthenticationProvider;
    private final TokenService tokenService;
    private final UserDtoMapper userDtoMapper;
    private final EmailVerificationService emailVerificationService;
    
    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;


    @Operation(summary = "Logowanie użytkownika", description = "Loguje użytkownika i zwraca JWT token w cookie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logowanie pomyślne",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "OK",
                                      "statusCode": 200,
                                      "reason": "User login request",
                                      "message": "Login successful",
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
            @ApiResponse(responseCode = "400", description = "Błąd walidacji lub nieprawidłowe dane logowania",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "BAD_REQUEST",
                                      "statusCode": 400,
                                      "reason": "Validation failed",
                                      "message": "Validation failed",
                                      "data": {
                                        "validationErrors": {
                                          "email": "Email must be a valid email address",
                                          "password": "Password cannot be blank"
                                        }
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp - nieprawidłowe dane logowania",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "UNAUTHORIZED",
                                      "statusCode": 401,
                                      "reason": "Authorization failed",
                                      "message": "Invalid credentials"
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "Email niezweryfikowany - wymagana weryfikacja emaila",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "FORBIDDEN",
                                      "statusCode": 403,
                                      "reason": "Email not verified",
                                      "message": "Email address is not verified. Please check your email and verify your account."
                                    }
                                    """)))
    })
    @PostMapping("/login")
    public ResponseEntity<HttpResponse> login(@Valid @RequestBody UserLoginRequest userLoginRequest, HttpServletResponse response) {
        try {
            final Authentication authentication = accountAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(userLoginRequest.email(), userLoginRequest.password()));

            final User user = userService.getUserByEmail(userLoginRequest.email());
            final String token = tokenService.generateJwtToken(authentication, user);

            // Ustaw JWT w cookie
            Cookie jwtCookie = new Cookie("jwt", token);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(24 * 60 * 60); // 1 dzień
            //jwtCookie.setSecure(false); // todo: true na produkcji (HTTPS)
            response.addCookie(jwtCookie);

            return ResponseEntity.status(OK).body(HttpResponse.builder()
                    .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                    .statusCode(OK.value())
                    .httpStatus(OK)
                    .reason("User login request")
                    .message("Login successful")
                    .data(Map.of("user", userDtoMapper.toDto(user)))
                    .build());
        } catch (EmailNotVerifiedException e) {
            // KROK 3: Email niezweryfikowany (sprawdzane po walidacji i sprawdzeniu hasła)
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).body(HttpResponse.builder()
                    .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                    .statusCode(org.springframework.http.HttpStatus.FORBIDDEN.value())
                    .httpStatus(org.springframework.http.HttpStatus.FORBIDDEN)
                    .reason("Email not verified")
                    .message(e.getMessage())
                    .build());
        } catch (AuthenticationException e) {
            throw new IllegalAccountAccessException(e.getMessage());
        }
    }

    @Operation(summary = "Rejestracja użytkownika", description = "Rejestruje nowego użytkownika w systemie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Użytkownik zarejestrowany pomyślnie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "CREATED",
                                      "statusCode": 201,
                                      "reason": "User register request",
                                      "message": "User registered successfully",
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
            @ApiResponse(responseCode = "400", description = "Błąd walidacji lub użytkownik już istnieje",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "BAD_REQUEST",
                                      "statusCode": 400,
                                      "reason": "User already exist",
                                      "message": "User with email user@example.com already exists"
                                    }
                                    """)))
    })
    @PostMapping("/register")
    public ResponseEntity<HttpResponse> register(@Valid @RequestBody UserRegisterRequest userRegisterRequest) {
        final User user = userService.registerUser(userRegisterRequest, UserType.USER);
        return ResponseEntity.status(CREATED).body(HttpResponse.builder()
                .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                .statusCode(CREATED.value())
                .httpStatus(CREATED)
                .reason("User register request")
                .message("User registered successfully")
                .data(Map.of("user", userDtoMapper.toDto(user)))
                .build());
    }

    @Operation(summary = "Pobierz dane zalogowanego użytkownika", description = "Zwraca dane aktualnie zalogowanego użytkownika")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dane użytkownika pobrane pomyślnie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "OK",
                                      "statusCode": 200,
                                      "reason": "User data request",
                                      "message": "User data retrieved successfully",
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
            @ApiResponse(responseCode = "404", description = "Użytkownik nie znaleziony",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "NOT_FOUND",
                                      "statusCode": 404,
                                      "reason": "User not found",
                                      "message": "User not found by email"
                                    }
                                    """)))
    })
    @GetMapping("/me")
    public ResponseEntity<HttpResponse> me(Authentication authentication) {
        final User user = userService.getUserByEmail(authentication.getName());

        return ResponseEntity.status(OK).body(HttpResponse.builder()
                .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                .statusCode(OK.value())
                .httpStatus(OK)
                .reason("User data request")
                .message("User data retrieved successfully")
                .data(Map.of("user", userDtoMapper.toDto(user)))
                .build());
    }

    @Operation(summary = "Zmiana hasła", description = "Zmienia hasło zalogowanego użytkownika")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hasło zmienione pomyślnie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "OK",
                                      "statusCode": 200,
                                      "reason": "Password change request",
                                      "message": "Password changed successfully"
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Błąd walidacji lub nieprawidłowe stare hasło",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "BAD_REQUEST",
                                      "statusCode": 400,
                                      "reason": "Validation failed",
                                      "message": "Validation failed",
                                      "data": {
                                        "validationErrors": {
                                          "oldPassword": "Old password cannot be blank",
                                          "newPassword": "New password must be at least 8 characters"
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
                                    """)))
    })
    @PostMapping("/change-password")
    public ResponseEntity<HttpResponse> changePassword(
            @RequestBody @Valid ChangePasswordRequest changePasswordRequest,
            Authentication authentication
    ) {

        final User user = userService.getUserByEmail(authentication.getName());
        userService.changeUserPassword(user.getId(), changePasswordRequest);

        return ResponseEntity.status(OK).body(HttpResponse.builder()
                .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                .statusCode(OK.value())
                .httpStatus(OK)
                .reason("Password change request")
                .message("Password changed successfully")
                .build());
    }

    @Operation(summary = "Wylogowanie użytkownika", description = "Wylogowuje użytkownika poprzez usunięcie JWT cookie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wylogowanie pomyślne",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "OK",
                                      "statusCode": 200,
                                      "reason": "Logout",
                                      "message": "Wylogowano pomyślnie"
                                    }
                                    """)))
    })
    @PostMapping("/logout")
    public ResponseEntity<HttpResponse> logout(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("jwt", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0); // Usuwa cookie
        //jwtCookie.setSecure(false); // true na produkcji (HTTPS)
        response.addCookie(jwtCookie);
        return ResponseEntity.ok(HttpResponse.builder()
                .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                .statusCode(200)
                .httpStatus(org.springframework.http.HttpStatus.OK)
                .reason("Logout")
                .message("Wylogowano pomyślnie")
                .build());
    }

    @Operation(summary = "Weryfikuj email", description = "Weryfikuje adres email użytkownika na podstawie tokenu")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email zweryfikowany pomyślnie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "OK",
                                      "statusCode": 200,
                                      "reason": "Email verification",
                                      "message": "Email verified successfully"
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowy lub wygasły token",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "BAD_REQUEST",
                                      "statusCode": 400,
                                      "reason": "Invalid token",
                                      "message": "Invalid or expired verification token"
                                    }
                                    """)))
    })
    @GetMapping("/verify-email")
    public RedirectView verifyEmail(@RequestParam("token") String token) {
        try {
            emailVerificationService.verifyEmail(token);
            // Przekieruj na frontend z komunikatem sukcesu
            RedirectView redirectView = new RedirectView();
            redirectView.setUrl(frontendUrl + "/verify-email?success=true");
            return redirectView;
        } catch (IllegalArgumentException e) {
            // Przekieruj na frontend z komunikatem błędu
            RedirectView redirectView = new RedirectView();
            redirectView.setUrl(frontendUrl + "/verify-email?error=" + e.getMessage().replace(" ", "%20"));
            return redirectView;
        }
    }

    @Operation(summary = "Wyślij ponownie email weryfikacyjny", description = "Wysyła ponownie email weryfikacyjny na podany adres")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email weryfikacyjny wysłany",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "OK",
                                      "statusCode": 200,
                                      "reason": "Resend verification email",
                                      "message": "Verification email sent"
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Email już zweryfikowany lub użytkownik nie istnieje",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "BAD_REQUEST",
                                      "statusCode": 400,
                                      "reason": "Invalid request",
                                      "message": "Email is already verified"
                                    }
                                    """)))
    })
    @PostMapping("/resend-verification")
    public ResponseEntity<HttpResponse> resendVerificationEmail(@RequestParam("email") String email) {
        try {
            emailVerificationService.resendVerificationEmail(email);
            return ResponseEntity.status(OK).body(HttpResponse.builder()
                    .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                    .statusCode(OK.value())
                    .httpStatus(OK)
                    .reason("Resend verification email")
                    .message("Verification email sent")
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST).body(HttpResponse.builder()
                    .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                    .statusCode(org.springframework.http.HttpStatus.BAD_REQUEST.value())
                    .httpStatus(org.springframework.http.HttpStatus.BAD_REQUEST)
                    .reason("Invalid request")
                    .message(e.getMessage())
                    .build());
        }
    }
}
