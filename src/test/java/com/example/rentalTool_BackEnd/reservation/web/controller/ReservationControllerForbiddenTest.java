package com.example.rentalTool_BackEnd.reservation.web.controller;

import com.example.rentalTool_BackEnd.reservation.model.Reservation;
import com.example.rentalTool_BackEnd.reservation.service.ReservationService;
import com.example.rentalTool_BackEnd.tool.spi.TermsExternalService;
import com.example.rentalTool_BackEnd.tool.category.model.Category;
import com.example.rentalTool_BackEnd.tool.spi.ToolExternalDto;
import com.example.rentalTool_BackEnd.tool.spi.ToolExternalService;
import com.example.rentalTool_BackEnd.user.spi.UserExternalService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ReservationControllerForbiddenTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservationService reservationService;

    @MockBean
    private ToolExternalService toolExternalService;

    @MockBean
    private TermsExternalService termsExternalService;

    @MockBean
    private UserExternalService userExternalService;

    private ToolExternalDto toolDto;
    private Reservation reservation;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = createTestCategory();
        toolDto = new ToolExternalDto(1L, 1L, 10.0, testCategory, true, 1L);
        reservation = new Reservation(1L, 2L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));
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
    void testConfirmReservation_WhenNotOwner_ShouldReturn403() throws Exception {
        // Próba potwierdzenia rezerwacji przez użytkownika, który nie jest właścicielem narzędzia
        reservation.confirm();
        
        when(reservationService.getReservationById(1L)).thenReturn(reservation);
        when(toolExternalService.getToolDtoById(1L)).thenReturn(toolDto);
        // toolDto.ownerId() = 1L, ale użytkownik = 99L (nie jest właścicielem)

        mockMvc.perform(put("/api/v1/reservations/1/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(createMockAuthentication(99L)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusCode").value(403))
                .andExpect(jsonPath("$.httpStatus").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("You are not the owner of this tool"));
    }

    @Test
    void testAcceptRegulationsReservation_WhenNotRenter_ShouldReturn403() throws Exception {
        // Próba zaakceptowania regulaminu rezerwacji przez użytkownika, który nie jest najemcą
        reservation.confirm();
        
        when(reservationService.getReservationById(1L)).thenReturn(reservation);
        when(toolExternalService.getToolDtoById(1L)).thenReturn(toolDto);
        // reservation.renterId() = 2L, ale użytkownik = 99L (nie jest najemcą)

        String requestBody = """
                {
                    "termsAccepted": true
                }
                """;

        mockMvc.perform(put("/api/v1/reservations/1/accept-regulations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .principal(createMockAuthentication(99L)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusCode").value(403))
                .andExpect(jsonPath("$.httpStatus").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("You are not the renter of this tool"));
    }

    @Test
    void testCancelReservation_WhenNotRenter_ShouldReturn403() throws Exception {
        // Próba anulowania rezerwacji przez użytkownika, który nie jest najemcą
        when(reservationService.getReservationById(1L)).thenReturn(reservation);
        // reservation.renterId() = 2L, ale użytkownik = 99L (nie jest najemcą)

        mockMvc.perform(put("/api/v1/reservations/1/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(createMockAuthentication(99L)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusCode").value(403))
                .andExpect(jsonPath("$.httpStatus").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("You are not the renter of this tool"));
    }

    @Test
    void testGetReservationById_WhenNotRenterOrOwner_ShouldReturn403() throws Exception {
        // Próba pobrania rezerwacji przez użytkownika, który nie jest ani najemcą, ani właścicielem
        when(reservationService.getReservationById(1L)).thenReturn(reservation);
        when(toolExternalService.getToolDtoById(1L)).thenReturn(toolDto);
        // reservation.renterId() = 2L, toolDto.ownerId() = 1L, ale użytkownik = 99L

        mockMvc.perform(get("/api/v1/reservations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(createMockAuthentication(99L)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusCode").value(403))
                .andExpect(jsonPath("$.httpStatus").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("You are not the renter or owner of this tool"));
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

