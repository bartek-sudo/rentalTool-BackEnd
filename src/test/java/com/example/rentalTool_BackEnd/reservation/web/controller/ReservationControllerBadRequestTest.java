package com.example.rentalTool_BackEnd.reservation.web.controller;

import com.example.rentalTool_BackEnd.reservation.model.Reservation;
import com.example.rentalTool_BackEnd.reservation.model.Terms;
import com.example.rentalTool_BackEnd.reservation.service.ReservationService;
import com.example.rentalTool_BackEnd.reservation.service.TermsService;
import com.example.rentalTool_BackEnd.tool.model.enums.Category;
import com.example.rentalTool_BackEnd.tool.spi.ToolExternalDto;
import com.example.rentalTool_BackEnd.tool.spi.ToolExternalService;
import com.example.rentalTool_BackEnd.user.service.UserService;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ReservationControllerBadRequestTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservationService reservationService;

    @MockBean
    private ToolExternalService toolExternalService;

    @MockBean
    private TermsService termsService;

    @MockBean
    private UserService userService;

    private ToolExternalDto toolDto;
    @BeforeEach
    void setUp() {
        toolDto = new ToolExternalDto(1L, 1L, 10.0, Category.OTHER, true, 1L);
    }

    @Test
    void testCreateReservation_WithOwnTool_ShouldReturn400() throws Exception {
        // Użytkownik próbuje zarezerwować własne narzędzie
        long ownerId = 1L;
        ToolExternalDto ownTool = new ToolExternalDto(1L, ownerId, 10.0, Category.OTHER, true, 1L);

        when(toolExternalService.getToolDtoById(1L)).thenReturn(ownTool);

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
                        .principal(createMockAuthentication(ownerId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("You cannot reserve your own tool"));
    }

    @Test
    void testConfirmReservation_WithWrongStatus_ShouldReturn400() throws Exception {
        // Próba potwierdzenia rezerwacji, która nie jest w statusie PENDING
        Reservation confirmedReservation = new Reservation(1L, 2L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));
        confirmedReservation.confirm(); // Zmienia status na CONFIRMED
        
        when(reservationService.getReservationById(1L)).thenReturn(confirmedReservation);
        when(toolExternalService.getToolDtoById(1L)).thenReturn(toolDto);

        mockMvc.perform(put("/api/v1/reservations/1/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(createMockAuthentication(1L)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Reservation is not in pending status"));
    }

    @Test
    void testAcceptRegulationsReservation_WithWrongStatus_ShouldReturn400() throws Exception {
        // Próba zaakceptowania regulaminu rezerwacji, która nie jest w statusie CONFIRMED (jest PENDING)
        Reservation pendingReservation = new Reservation(1L, 2L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));
        // Status jest już PENDING z konstruktora
        
        when(reservationService.getReservationById(1L)).thenReturn(pendingReservation);
        when(toolExternalService.getToolDtoById(1L)).thenReturn(toolDto);
        Terms terms = new Terms();
        terms.setId(1L);
        when(termsService.getTermsById(1L)).thenReturn(terms);

        String requestBody = """
                {
                    "termsAccepted": true
                }
                """;

        mockMvc.perform(put("/api/v1/reservations/1/accept-regulations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .principal(createMockAuthentication(2L)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Reservation is not in confirmed status"));
    }

    @Test
    void testCancelReservation_WithWrongStatus_ShouldReturn400() throws Exception {
        // Próba anulowania rezerwacji, która jest w statusie REGULATIONS_ACCEPTED (można anulować tylko PENDING lub CONFIRMED)
        Reservation regulationsAcceptedReservation = new Reservation(1L, 2L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));
        regulationsAcceptedReservation.confirm();
        regulationsAcceptedReservation.acceptRegulations(1L); // Status REGULATIONS_ACCEPTED
        
        when(reservationService.getReservationById(1L)).thenReturn(regulationsAcceptedReservation);

        mockMvc.perform(put("/api/v1/reservations/1/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(createMockAuthentication(2L)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Reservation is not in pending or confirmed status"));
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

