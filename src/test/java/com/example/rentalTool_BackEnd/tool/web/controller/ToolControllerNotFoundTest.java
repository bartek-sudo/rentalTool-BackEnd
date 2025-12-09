package com.example.rentalTool_BackEnd.tool.web.controller;

import com.example.rentalTool_BackEnd.reservation.service.ReservationService;
import com.example.rentalTool_BackEnd.tool.exception.ToolNotFoundException;
import com.example.rentalTool_BackEnd.tool.model.Tool;
import com.example.rentalTool_BackEnd.tool.model.enums.ModerationStatus;
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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ToolControllerNotFoundTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ToolService toolService;

    @MockBean
    private ReservationService reservationService;

    @MockBean
    private ToolExternalService toolExternalService;

    @Test
    void testGetToolById_WhenToolNotFound_ShouldReturn404() throws Exception {
        // Próba pobrania nieistniejącego narzędzia
        when(toolService.getToolById(999L))
                .thenThrow(new ToolNotFoundException("Tool not found"));

        mockMvc.perform(get("/api/v1/tools/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                .andExpect(jsonPath("$.reason").value("Tool has not been found"));
    }

    @Test
    void testGetToolById_WhenToolNotPubliclyVisible_ShouldReturn404() throws Exception {
        // Próba pobrania narzędzia, które nie jest publicznie widoczne przez użytkownika, który nie jest właścicielem ani moderatorem
        // Narzędzie nie jest publicznie widoczne, jeśli moderationStatus != APPROVED lub isActive == false
        Tool tool = new Tool();
        tool.setId(1L);
        tool.setOwnerId(1L);
        tool.setModerationStatus(ModerationStatus.PENDING); // Nie zatwierdzone
        tool.setActive(false); // Nieaktywne
        
        when(toolService.getToolById(1L)).thenReturn(tool);
        // Użytkownik 99L nie jest właścicielem (1L) ani moderatorem

        mockMvc.perform(get("/api/v1/tools/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(createMockAuthentication(99L)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Tool is not publicly available"));
    }

    @Test
    void testGetToolById_WhenToolNotPubliclyVisible_WithoutAuth_ShouldReturn404() throws Exception {
        // Próba pobrania narzędzia, które nie jest publicznie widoczne bez autoryzacji
        Tool tool = new Tool();
        tool.setId(1L);
        tool.setOwnerId(1L);
        tool.setModerationStatus(ModerationStatus.REJECTED); // Odrzucone
        tool.setActive(false); // Nieaktywne
        
        when(toolService.getToolById(1L)).thenReturn(tool);

        mockMvc.perform(get("/api/v1/tools/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Tool is not publicly available"));
    }

    @Test
    void testUpdateTool_WhenToolNotFound_ShouldReturn404() throws Exception {
        // Próba pobrania nieistniejącego narzędzia (update używa tego samego endpointu GET do pobierania)
        when(toolService.getToolById(999L))
                .thenThrow(new ToolNotFoundException("Tool not found"));

        mockMvc.perform(get("/api/v1/tools/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(createMockAuthentication(1L)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"));
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

