package com.example.rentalTool_BackEnd.user.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testRegister_WithInvalidEmail_ShouldReturn400() throws Exception {
        String invalidRequest = """
                {
                    "email": "invalid-email",
                    "password": "password123",
                    "firstName": "Jan",
                    "lastName": "Kowalski"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.data.validationErrors.email").exists());
    }

    @Test
    void testRegister_WithBlankEmail_ShouldReturn400() throws Exception {
        String invalidRequest = """
                {
                    "email": "",
                    "password": "password123",
                    "firstName": "Jan",
                    "lastName": "Kowalski"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data.validationErrors.email").exists());
    }

    @Test
    void testRegister_WithNullEmail_ShouldReturn400() throws Exception {
        String invalidRequest = """
                {
                    "email": null,
                    "password": "password123",
                    "firstName": "Jan",
                    "lastName": "Kowalski"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data.validationErrors.email").exists());
    }

    @Test
    void testRegister_WithShortPassword_ShouldReturn400() throws Exception {
        String invalidRequest = """
                {
                    "email": "test@example.com",
                    "password": "12345",
                    "firstName": "Jan",
                    "lastName": "Kowalski"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data.validationErrors.password").exists());
    }

    @Test
    void testRegister_WithBlankPassword_ShouldReturn400() throws Exception {
        String invalidRequest = """
                {
                    "email": "test@example.com",
                    "password": "",
                    "firstName": "Jan",
                    "lastName": "Kowalski"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data.validationErrors.password").exists());
    }

    @Test
    void testRegister_WithBlankFirstName_ShouldReturn400() throws Exception {
        String invalidRequest = """
                {
                    "email": "test@example.com",
                    "password": "password123",
                    "firstName": "",
                    "lastName": "Kowalski"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data.validationErrors.firstName").exists());
    }

    @Test
    void testRegister_WithBlankLastName_ShouldReturn400() throws Exception {
        String invalidRequest = """
                {
                    "email": "test@example.com",
                    "password": "password123",
                    "firstName": "Jan",
                    "lastName": ""
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data.validationErrors.lastName").exists());
    }

    @Test
    void testRegister_WithMultipleValidationErrors_ShouldReturn400() throws Exception {
        String invalidRequest = """
                {
                    "email": "invalid-email",
                    "password": "123",
                    "firstName": "",
                    "lastName": ""
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data.validationErrors.email").exists())
                .andExpect(jsonPath("$.data.validationErrors.password").exists())
                .andExpect(jsonPath("$.data.validationErrors.firstName").exists())
                .andExpect(jsonPath("$.data.validationErrors.lastName").exists());
    }

    @Test
    void testLogin_WithInvalidEmail_ShouldReturn400() throws Exception {
        String invalidRequest = """
                {
                    "email": "invalid-email",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data.validationErrors.email").exists());
    }

    @Test
    void testLogin_WithBlankPassword_ShouldReturn400() throws Exception {
        String invalidRequest = """
                {
                    "email": "test@example.com",
                    "password": ""
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data.validationErrors.password").exists());
    }

    @Test
    void testLogin_WithShortPassword_ShouldReturn400() throws Exception {
        String invalidRequest = """
                {
                    "email": "test@example.com",
                    "password": "12345"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data.validationErrors.password").exists());
    }

    @Test
    void testChangePassword_WithBlankOldPassword_ShouldReturn400() throws Exception {
        String invalidRequest = """
                {
                    "oldPassword": "",
                    "newPassword": "newpassword123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data.validationErrors.oldPassword").exists());
    }

    @Test
    void testChangePassword_WithShortNewPassword_ShouldReturn400() throws Exception {
        String invalidRequest = """
                {
                    "oldPassword": "oldpassword123",
                    "newPassword": "12345"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data.validationErrors.newPassword").exists());
    }

    @Test
    void testChangePassword_WithBothPasswordsInvalid_ShouldReturn400() throws Exception {
        String invalidRequest = """
                {
                    "oldPassword": "",
                    "newPassword": "123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data.validationErrors.oldPassword").exists())
                .andExpect(jsonPath("$.data.validationErrors.newPassword").exists());
    }
}

