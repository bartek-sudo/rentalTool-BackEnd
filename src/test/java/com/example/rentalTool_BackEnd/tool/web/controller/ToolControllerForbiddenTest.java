package com.example.rentalTool_BackEnd.tool.web.controller;

import com.example.rentalTool_BackEnd.reservation.service.ReservationService;
import com.example.rentalTool_BackEnd.tool.exception.UnauthorizedToolAccessException;
import com.example.rentalTool_BackEnd.tool.service.ToolService;
import com.example.rentalTool_BackEnd.tool.spi.ToolExternalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ToolControllerForbiddenTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ToolService toolService;

    @MockBean
    private ReservationService reservationService;

    @MockBean
    private ToolExternalService toolExternalService;

    @Test
    void testUpdateTool_WhenNotAuthorized_ShouldReturn403() throws Exception {
        // Próba aktualizacji narzędzia przez użytkownika, który nie jest właścicielem
        when(toolService.updateTool(anyLong(), any(), anyLong()))
                .thenThrow(new UnauthorizedToolAccessException("You are not authorized to update this tool"));

        String requestBody = """
                {
                    "name": "Updated Tool",
                    "description": "Updated description",
                    "pricePerDay": 15.0,
                    "category": "OTHER",
                    "address": "Updated address",
                    "latitude": 52.0,
                    "longitude": 21.0
                }
                """;

        mockMvc.perform(put("/api/v1/tools/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .principal(createMockAuthentication(99L)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusCode").value(403))
                .andExpect(jsonPath("$.httpStatus").value("FORBIDDEN"))
                .andExpect(jsonPath("$.reason").value("Unauthorized access to tool"))
                .andExpect(jsonPath("$.message").value("You are not authorized to update this tool"));
    }

    @Test
    void testSetToolStatus_WhenNotAuthorized_ShouldReturn403() throws Exception {
        // Próba zmiany statusu narzędzia przez użytkownika, który nie jest właścicielem
        when(toolService.setToolStatus(anyLong(), anyLong(), any(Boolean.class)))
                .thenThrow(new UnauthorizedToolAccessException("You can only change status of your own tools"));

        mockMvc.perform(patch("/api/v1/tools/1/status?active=true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(createMockAuthentication(99L)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusCode").value(403))
                .andExpect(jsonPath("$.httpStatus").value("FORBIDDEN"))
                .andExpect(jsonPath("$.reason").value("Unauthorized access to tool"))
                .andExpect(jsonPath("$.message").value("You can only change status of your own tools"));
    }

    private org.springframework.security.core.Authentication createMockAuthentication(long userId) {
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("user_id", userId)
                .claim("sub", "user@example.com")
                .claim("authorities", Collections.emptyList())
                .build();
        return new JwtAuthenticationToken(jwt);
    }
}

