package com.example.rentalTool_BackEnd.user.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerUnauthorizedTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetMe_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Próba pobrania danych użytkownika bez autoryzacji
        mockMvc.perform(get("/api/v1/auth/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testChangePassword_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Próba zmiany hasła bez autoryzacji
        String requestBody = """
                {
                    "oldPassword": "oldpassword123",
                    "newPassword": "newpassword123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLogout_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Próba wylogowania bez autoryzacji (jeśli endpoint wymaga autoryzacji)
        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetMe_WithInvalidToken_ShouldReturn401() throws Exception {
        // Próba pobrania danych użytkownika z nieprawidłowym tokenem
        mockMvc.perform(get("/api/v1/auth/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new jakarta.servlet.http.Cookie("jwt", "invalid-token")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value(401))
                .andExpect(jsonPath("$.httpStatus").value("UNAUTHORIZED"));
    }
}


