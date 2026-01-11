package com.example.rentalTool_BackEnd.reservation.web.controller;

import com.example.rentalTool_BackEnd.reservation.exception.ToolNotAvailableException;
import com.example.rentalTool_BackEnd.reservation.service.ReservationService;
import com.example.rentalTool_BackEnd.tool.category.model.Category;
import com.example.rentalTool_BackEnd.tool.service.ToolService;
import com.example.rentalTool_BackEnd.tool.spi.ToolExternalDto;
import com.example.rentalTool_BackEnd.tool.spi.ToolExternalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ReservationExceptionBadRequestTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservationService reservationService;

    @MockBean
    private ToolExternalService toolExternalService;

    @MockBean
    private ToolService toolService;

    private ToolExternalDto toolDto;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = createTestCategory();
        toolDto = new ToolExternalDto(1L, 1L, 10.0, testCategory, true, 1L);
    }

    private Category createTestCategory() {
        Category category = new Category();
        category.setId(1L);
        category.setName("OTHER");
        category.setDisplayName("Inne");
        category.setDescription("Pozostałe narzędzia");
        return category;
    }

    @Test
    void testCreateReservation_WhenToolNotAvailable_ShouldReturn400() throws Exception {
        // Narzędzie nie jest dostępne w wybranym okresie
        when(toolExternalService.getToolDtoById(1L)).thenReturn(toolDto);
        when(reservationService.createReservation(anyLong(), anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenThrow(new ToolNotAvailableException("Tool is not available for the selected dates"));

        String requestBody = """
                {
                    "toolId": 1,
                    "startDate": "2025-12-01",
                    "endDate": "2025-12-05"
                }
                """;

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .principal(createMockAuthentication(2L)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.reason").value("Tool is not available for the requested period"))
                .andExpect(jsonPath("$.message").value("Tool is not available for the selected dates"));
    }

    @Test
    void testCreateReservation_WhenToolNoLongerAvailable_ShouldReturn400() throws Exception {
        // Narzędzie przestało być dostępne (np. zostało usunięte lub zablokowane)
        when(toolExternalService.getToolDtoById(1L)).thenReturn(toolDto);
        when(reservationService.createReservation(anyLong(), anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenThrow(new ToolNotAvailableException("Tool is no longer available for booking"));

        String requestBody = """
                {
                    "toolId": 1,
                    "startDate": "2025-12-01",
                    "endDate": "2025-12-05"
                }
                """;

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .principal(createMockAuthentication(2L)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.reason").value("Tool is not available for the requested period"))
                .andExpect(jsonPath("$.message").value("Tool is no longer available for booking"));
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

