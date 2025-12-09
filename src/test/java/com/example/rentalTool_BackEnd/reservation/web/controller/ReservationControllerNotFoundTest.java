package com.example.rentalTool_BackEnd.reservation.web.controller;

import com.example.rentalTool_BackEnd.reservation.exception.ReservationNotFoundException;
import com.example.rentalTool_BackEnd.reservation.service.ReservationService;
import com.example.rentalTool_BackEnd.tool.spi.ToolExternalService;
import com.example.rentalTool_BackEnd.user.service.UserService;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ReservationControllerNotFoundTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservationService reservationService;

    @MockBean
    private ToolExternalService toolExternalService;

    @MockBean
    private UserService userService;

    @Test
    void testConfirmReservation_WhenReservationNotFound_ShouldReturn404() throws Exception {
        // Próba potwierdzenia nieistniejącej rezerwacji
        when(reservationService.getReservationById(999L))
                .thenThrow(new ReservationNotFoundException("Reservation not found"));

        mockMvc.perform(put("/api/v1/reservations/999/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(createMockAuthentication(1L)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                .andExpect(jsonPath("$.reason").value("Reservation has not been found"));
    }

    @Test
    void testAcceptRegulationsReservation_WhenReservationNotFound_ShouldReturn404() throws Exception {
        // Próba zaakceptowania regulaminu nieistniejącej rezerwacji
        when(reservationService.getReservationById(999L))
                .thenThrow(new ReservationNotFoundException("Reservation not found"));

        String requestBody = """
                {
                    "termsAccepted": true
                }
                """;

        mockMvc.perform(put("/api/v1/reservations/999/accept-regulations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .principal(createMockAuthentication(1L)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"));
    }

    @Test
    void testCancelReservation_WhenReservationNotFound_ShouldReturn404() throws Exception {
        // Próba anulowania nieistniejącej rezerwacji
        when(reservationService.getReservationById(999L))
                .thenThrow(new ReservationNotFoundException("Reservation not found"));

        mockMvc.perform(put("/api/v1/reservations/999/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(createMockAuthentication(1L)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"));
    }

    @Test
    void testGetReservationById_WhenReservationNotFound_ShouldReturn404() throws Exception {
        // Próba pobrania nieistniejącej rezerwacji
        when(reservationService.getReservationById(999L))
                .thenThrow(new ReservationNotFoundException("Reservation not found"));

        mockMvc.perform(get("/api/v1/reservations/999")
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

