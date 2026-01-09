package com.example.rentalTool_BackEnd.tool.web.controller;

import com.example.rentalTool_BackEnd.reservation.service.ReservationService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ToolControllerIllegalArgumentExceptionTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ToolService toolService;

    @MockBean
    private ReservationService reservationService;

    @MockBean
    private ToolExternalService toolExternalService;

    @Test
    void testRejectTool_WithoutComment_ShouldReturn400() throws Exception {
        // Próba odrzucenia narzędzia bez komentarza (wymagany)
        when(toolService.rejectTool(anyLong(), anyLong(), any()))
                .thenThrow(new IllegalArgumentException("Rejection comment is required"));

        String requestBody = """
                {
                    "comment": ""
                }
                """;

        mockMvc.perform(post("/api/v1/moderation/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .principal(createMockAuthentication(1L)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.reason").value("Invalid argument"))
                .andExpect(jsonPath("$.message").value("Rejection comment is required"));
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

