package com.example.rentalTool_BackEnd.user.web.controller;

import com.example.rentalTool_BackEnd.shared.model.HttpResponse;
import com.example.rentalTool_BackEnd.shared.util.TimeUtil;
import com.example.rentalTool_BackEnd.user.exception.IllegalAccountAccessException;
import com.example.rentalTool_BackEnd.user.model.User;
import com.example.rentalTool_BackEnd.user.model.enums.UserType;
import com.example.rentalTool_BackEnd.user.security.jwt.service.TokenService;
import com.example.rentalTool_BackEnd.user.security.provider.AccountAuthenticationProvider;
import com.example.rentalTool_BackEnd.user.service.UserService;
import com.example.rentalTool_BackEnd.user.web.mapper.UserDtoMapper;
import com.example.rentalTool_BackEnd.user.web.requests.ChangePasswordRequest;
import com.example.rentalTool_BackEnd.user.web.requests.UserLoginRequest;
import com.example.rentalTool_BackEnd.user.web.requests.UserRegisterRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

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
        }catch (AuthenticationException e){
            throw new IllegalAccountAccessException(e.getMessage());
        }
    }

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
}
