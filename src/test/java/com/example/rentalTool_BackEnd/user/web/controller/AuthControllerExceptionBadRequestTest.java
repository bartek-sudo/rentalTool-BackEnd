package com.example.rentalTool_BackEnd.user.web.controller;

import com.example.rentalTool_BackEnd.user.exception.UserAlreadyExistException;
import com.example.rentalTool_BackEnd.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerExceptionBadRequestTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void testRegister_WithExistingEmail_ShouldReturn400() throws Exception {
        // Próba rejestracji użytkownika z już istniejącym emailem
        when(userService.registerUser(any(), any()))
                .thenThrow(new UserAlreadyExistException("User already exists"));

        String requestBody = """
                {
                    "email": "existing@example.com",
                    "password": "password123",
                    "firstName": "Jan",
                    "lastName": "Kowalski"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.reason").value("User already exist"))
                .andExpect(jsonPath("$.message").value("User already exists"));
    }
}

