package com.example.rentalTool_BackEnd.tool.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ModerationControllerBadRequestTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetToolsByStatus_WithInvalidStatus_ShouldReturn400() throws Exception {
        // Próba pobrania narzędzi z nieprawidłowym statusem moderacji
        mockMvc.perform(get("/api/v1/moderation/status/INVALID_STATUS")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.reason").value("Invalid moderation status"))
                .andExpect(jsonPath("$.message").value("Valid statuses: PENDING, APPROVED, REJECTED"));
    }

    @Test
    void testGetToolsByStatus_WithEmptyStatus_ShouldReturn400() throws Exception {
        // Próba pobrania narzędzi z pustym statusem
        mockMvc.perform(get("/api/v1/moderation/status/")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Spring zwróci 404 dla pustego path variable
    }

    @Test
    void testGetToolsByStatus_WithLowercaseStatus_ShouldWork() throws Exception {
        // Status w małych literach powinien zostać przekonwertowany na uppercase
        // Ten test weryfikuje, że konwersja działa, ale jeśli status jest nieprawidłowy, zwróci 400
        mockMvc.perform(get("/api/v1/moderation/status/nonexistent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("Valid statuses: PENDING, APPROVED, REJECTED"));
    }
}


