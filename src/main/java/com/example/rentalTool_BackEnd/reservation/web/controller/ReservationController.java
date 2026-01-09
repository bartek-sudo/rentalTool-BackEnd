package com.example.rentalTool_BackEnd.reservation.web.controller;

import com.example.rentalTool_BackEnd.reservation.model.Reservation;
import com.example.rentalTool_BackEnd.reservation.model.enums.ReservationStatus;
import com.example.rentalTool_BackEnd.reservation.service.ReservationService;
import com.example.rentalTool_BackEnd.reservation.web.mapper.ReservationMapper;
import com.example.rentalTool_BackEnd.reservation.web.requests.ReservationCreateRequest;
import com.example.rentalTool_BackEnd.reservation.web.requests.RegulationsAcceptRequest;
import com.example.rentalTool_BackEnd.shared.model.HttpResponse;
import com.example.rentalTool_BackEnd.tool.spi.TermsExternalService;
import com.example.rentalTool_BackEnd.tool.spi.ToolExternalDto;
import com.example.rentalTool_BackEnd.tool.spi.ToolExternalService;
import com.example.rentalTool_BackEnd.user.spi.UserExternalDto;
import com.example.rentalTool_BackEnd.user.spi.UserExternalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationService;
    private final ToolExternalService toolExternalService;
    private final ReservationMapper reservationMapper;
    private final UserExternalService userExternalService;
    private final TermsExternalService termsExternalService;

    @Operation(summary = "Utwórz rezerwację", description = "Tworzy nową rezerwację narzędzia")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Rezerwacja utworzona pomyślnie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "CREATED",
                                      "statusCode": 201,
                                      "reason": "Reservation created successfully",
                                      "message": "Reservation created",
                                      "data": {
                                        "reservation": {
                                          "id": 1,
                                          "toolId": 1,
                                          "renterId": 2,
                                          "startDate": "2025-12-01",
                                          "endDate": "2025-12-05",
                                          "status": "PENDING"
                                        }
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Błąd walidacji, próba rezerwacji własnego narzędzia lub narzędzie niedostępne",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "BAD_REQUEST",
                                      "statusCode": 400,
                                      "reason": "Bad Request",
                                      "message": "You cannot reserve your own tool"
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp - wymagane zalogowanie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "UNAUTHORIZED",
                                      "statusCode": 401,
                                      "reason": "Authorization failed",
                                      "message": "Unauthorized access - authentication required"
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Narzędzie nie znalezione",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "NOT_FOUND",
                                      "statusCode": 404,
                                      "reason": "Tool has not been found",
                                      "message": "Tool not found"
                                    }
                                    """)))
    })
    @PostMapping
    public ResponseEntity<HttpResponse> createReservation(
            @RequestBody ReservationCreateRequest request,
            Authentication authentication) {

        final Jwt jwt = (Jwt) authentication.getPrincipal();

        final long userId = jwt.getClaim("user_id");
        ToolExternalDto tool = toolExternalService.getToolDtoById(request.toolId());

        if (tool.ownerId() == userId) {
            return ResponseEntity.badRequest()
                    .body(HttpResponse.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .httpStatus(HttpStatus.BAD_REQUEST)
                            .reason("Bad Request")
                            .message("You cannot reserve your own tool")
                            .build());
        }

        final Reservation reservation = reservationService.createReservation(
                tool.id(), userId, request.startDate(), request.endDate());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(HttpResponse.builder()
                        .statusCode(HttpStatus.CREATED.value())
                        .httpStatus(HttpStatus.CREATED)
                        .reason("Reservation created successfully")
                        .message("Reservation created")
                        .data(Map.of("reservation", reservationMapper.toDto(reservation)))
                        .build());

    }

    @Operation(summary = "Pobierz moje wypożyczenia", description = "Zwraca listę rezerwacji utworzonych przez zalogowanego użytkownika (jako najemca)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista wypożyczeń pobrana pomyślnie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "OK",
                                      "statusCode": 200,
                                      "reason": "My rentals",
                                      "message": "My rentals",
                                      "data": {
                                        "rentals": [
                                          {
                                            "id": 1,
                                            "toolId": 10,
                                            "renterId": 2,
                                            "startDate": "2025-12-01",
                                            "endDate": "2025-12-05",
                                            "status": "PENDING"
                                          }
                                        ]
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp - wymagane zalogowanie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "UNAUTHORIZED",
                                      "statusCode": 401,
                                      "reason": "Authorization failed",
                                      "message": "Unauthorized access - authentication required"
                                    }
                                    """)))
    })
    @GetMapping("/my-rentals")
    public ResponseEntity<HttpResponse> getMyRentals(Authentication authentication) {
        final Jwt jwt = (Jwt) authentication.getPrincipal();

        final long userId = jwt.getClaim("user_id");
        final List<Reservation> reservations =  reservationService.getReservationsForRenter(userId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(HttpResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .httpStatus(HttpStatus.OK)
                        .reason("My rentals")
                        .message("My rentals")
                        .data(Map.of("rentals", reservations.stream()
                                .map(reservationMapper::toDto)
                                .toList()))
                        .build());
    }

    @Operation(summary = "Pobierz rezerwacje moich narzędzi", description = "Zwraca listę rezerwacji narzędzi należących do zalogowanego użytkownika (jako właściciel)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista rezerwacji pobrana pomyślnie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "OK",
                                      "statusCode": 200,
                                      "reason": "My tools reservations",
                                      "message": "My tools reservations",
                                      "data": {
                                        "reservations": [
                                          {
                                            "id": 5,
                                            "toolId": 1,
                                            "renterId": 10,
                                            "startDate": "2025-12-10",
                                            "endDate": "2025-12-15",
                                            "status": "CONFIRMED"
                                          }
                                        ]
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp - wymagane zalogowanie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "UNAUTHORIZED",
                                      "statusCode": 401,
                                      "reason": "Authorization failed",
                                      "message": "Unauthorized access - authentication required"
                                    }
                                    """)))
    })
    @GetMapping("/my-tools-reservations")
    public ResponseEntity<HttpResponse> getMyToolsReservations(Authentication authentication) {
        final Jwt jwt = (Jwt) authentication.getPrincipal();

        final long userId = jwt.getClaim("user_id");
        final List<Reservation> reservations =  reservationService.getReservationsForOwner(userId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(HttpResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .httpStatus(HttpStatus.OK)
                        .reason("My tools reservations")
                        .message("My tools reservations")
                        .data(Map.of("reservations", reservations.stream()
                                .map(reservationMapper::toDto)
                                .toList()))
                        .build());
    }

    @Operation(summary = "Potwierdź rezerwację", description = "Potwierdza rezerwację (tylko właściciel narzędzia)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rezerwacja potwierdzona pomyślnie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "OK",
                                      "statusCode": 200,
                                      "reason": "Reservation confirmed",
                                      "message": "Reservation confirmed",
                                      "data": {
                                        "reservation": {
                                          "id": 1,
                                          "toolId": 1,
                                          "renterId": 2,
                                          "status": "CONFIRMED"
                                        }
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Rezerwacja nie jest w statusie PENDING",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "BAD_REQUEST",
                                      "statusCode": 400,
                                      "reason": "Bad Request",
                                      "message": "Reservation is not in pending status"
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień - nie jesteś właścicielem narzędzia",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "FORBIDDEN",
                                      "statusCode": 403,
                                      "reason": "Forbidden",
                                      "message": "You are not the owner of this tool"
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Rezerwacja nie znaleziona",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "NOT_FOUND",
                                      "statusCode": 404,
                                      "reason": "Reservation has not been found",
                                      "message": "Reservation not found"
                                    }
                                    """)))
    })
    @PutMapping("/{reservationId}/confirm")
    public ResponseEntity<HttpResponse> confirmReservation(
            @PathVariable("reservationId") long reservationId,
            Authentication authentication) {

        final Jwt jwt = (Jwt) authentication.getPrincipal();

        final long userId = jwt.getClaim("user_id");
        Reservation reservation = reservationService.getReservationById(reservationId);
        final ToolExternalDto tool = toolExternalService.getToolDtoById(reservation.getToolId());

        if (tool.ownerId() != userId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(HttpResponse.builder()
                            .statusCode(HttpStatus.FORBIDDEN.value())
                            .httpStatus(HttpStatus.FORBIDDEN)
                            .reason("Forbidden")
                            .message("You are not the owner of this tool")
                            .build());
        }

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(HttpResponse.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .httpStatus(HttpStatus.BAD_REQUEST)
                            .reason("Bad Request")
                            .message("Reservation is not in pending status")
                            .build());
        }

        reservation = reservationService.confirmReservation(reservationId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(HttpResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .httpStatus(HttpStatus.OK)
                        .reason("Reservation confirmed")
                        .message("Reservation confirmed")
                        .data(Map.of("reservation", reservationMapper.toDto(reservation)))
                        .build());

    }

    @Operation(summary = "Zaakceptuj regulamin rezerwacji", description = "Potwierdza akceptację regulaminu - obie strony decydują się na dokonanie transakcji poza aplikacją (tylko najemca)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Regulamin zaakceptowany pomyślnie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "OK",
                                      "statusCode": 200,
                                      "reason": "Regulations accepted",
                                      "message": "Regulations accepted",
                                      "data": {
                                        "reservation": {
                                          "id": 1,
                                          "toolId": 1,
                                          "renterId": 2,
                                          "status": "REGULATIONS_ACCEPTED"
                                        },
                                        "contactInfo": {
                                          "renterEmail": "renter@example.com",
                                          "renterName": "Jan Kowalski",
                                          "ownerEmail": "owner@example.com",
                                          "ownerName": "Anna Nowak"
                                        }
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Rezerwacja nie jest w statusie CONFIRMED lub regulamin nie został zaakceptowany",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "BAD_REQUEST",
                                      "statusCode": 400,
                                      "reason": "Bad Request",
                                      "message": "Reservation is not in confirmed status or terms not accepted"
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień - nie jesteś najemcą",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "FORBIDDEN",
                                      "statusCode": 403,
                                      "reason": "Forbidden",
                                      "message": "You are not the renter of this tool"
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Rezerwacja nie znaleziona",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "NOT_FOUND",
                                      "statusCode": 404,
                                      "reason": "Reservation has not been found",
                                      "message": "Reservation not found"
                                    }
                                    """)))
    })
    @PutMapping("/{reservationId}/accept-regulations")
    public ResponseEntity<HttpResponse> acceptRegulationsReservation(
            @PathVariable("reservationId") long reservationId,
            @RequestBody RegulationsAcceptRequest request,
            Authentication authentication) {
        final Jwt jwt = (Jwt) authentication.getPrincipal();
        final long userId = jwt.getClaim("user_id");
        Reservation reservation = reservationService.getReservationById(reservationId);
        final ToolExternalDto tool = toolExternalService.getToolDtoById(reservation.getToolId());

        if (reservation.getRenterId() != userId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(HttpResponse.builder()
                            .statusCode(HttpStatus.FORBIDDEN.value())
                            .httpStatus(HttpStatus.FORBIDDEN)
                            .reason("Forbidden")
                            .message("You are not the renter of this tool")
                            .build());
        }

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(HttpResponse.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .httpStatus(HttpStatus.BAD_REQUEST)
                            .reason("Bad Request")
                            .message("Reservation is not in confirmed status")
                            .build());
        }

        if (!request.termsAccepted()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(HttpResponse.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .httpStatus(HttpStatus.BAD_REQUEST)
                            .reason("Bad Request")
                            .message("Terms must be accepted")
                            .build());
        }

        if (tool.termsId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(HttpResponse.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .httpStatus(HttpStatus.BAD_REQUEST)
                            .reason("Bad Request")
                            .message("Tool does not have a terms assigned by the owner")
                            .build());
        }

        try {
            termsExternalService.getTermsDtoById(tool.termsId());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(HttpResponse.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .httpStatus(HttpStatus.BAD_REQUEST)
                            .reason("Bad Request")
                            .message("Terms not found with id: " + tool.termsId())
                            .build());
        }

        reservation = reservationService.acceptRegulationsReservation(reservationId, tool.termsId());

        // Pobierz dane kontaktowe najemcy i właściciela
        UserExternalDto renter = userExternalService.getUserDtoById(reservation.getRenterId());
        UserExternalDto owner = userExternalService.getUserDtoById(tool.ownerId());

        Map<String, Object> contactInfo = Map.of(
                "renterEmail", renter.email(),
                "renterName", renter.firstName() + " " + renter.lastName(),
                "renterPhoneNumber", renter.phoneNumber() != null ? renter.phoneNumber() : "Nie podano",
                "ownerEmail", owner.email(),
                "ownerName", owner.firstName() + " " + owner.lastName(),
                "ownerPhoneNumber", owner.phoneNumber() != null ? owner.phoneNumber() : "Nie podano"
        );
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(HttpResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .httpStatus(HttpStatus.OK)
                        .reason("Regulations accepted")
                        .message("Regulations accepted - both parties agree to complete the transaction outside the application")
                        .data(Map.of(
                                "reservation", reservationMapper.toDto(reservation),
                                "contactInfo", contactInfo
                        ))
                        .build());

    }

    @Operation(summary = "Anuluj rezerwację", description = "Anuluje rezerwację (PENDING i CONFIRMED: obie strony mogą anulować)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rezerwacja anulowana pomyślnie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "OK",
                                      "statusCode": 200,
                                      "reason": "Reservation canceled",
                                      "message": "Reservation canceled",
                                      "data": {
                                        "reservation": {
                                          "id": 1,
                                          "toolId": 1,
                                          "renterId": 2,
                                          "status": "CANCELED"
                                        }
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Rezerwacja nie jest w statusie PENDING lub CONFIRMED",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "BAD_REQUEST",
                                      "statusCode": 400,
                                      "reason": "Bad Request",
                                      "message": "Reservation is not in pending or confirmed status"
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień do anulowania rezerwacji",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "FORBIDDEN",
                                      "statusCode": 403,
                                      "reason": "Forbidden",
                                      "message": "You don't have permission to cancel this reservation"
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Rezerwacja nie znaleziona",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "NOT_FOUND",
                                      "statusCode": 404,
                                      "reason": "Reservation has not been found",
                                      "message": "Reservation not found"
                                    }
                                    """)))
    })
    @PutMapping("/{reservationId}/cancel")
    public ResponseEntity<HttpResponse> cancelReservation(
            @PathVariable("reservationId") long reservationId,
            Authentication authentication) {
        final Jwt jwt = (Jwt) authentication.getPrincipal();
        final long userId = jwt.getClaim("user_id");
        Reservation reservation = reservationService.getReservationById(reservationId);
        final ToolExternalDto tool = toolExternalService.getToolDtoById(reservation.getToolId());

        // Sprawdzenie statusu rezerwacji
        if (reservation.getStatus() != ReservationStatus.PENDING &&
                reservation.getStatus() != ReservationStatus.CONFIRMED) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(HttpResponse.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .httpStatus(HttpStatus.BAD_REQUEST)
                            .reason("Bad Request")
                            .message("Reservation is not in pending or confirmed status")
                            .build());
        }

        // Logika uprawnień w zależności od statusu
        if (reservation.getStatus() == ReservationStatus.PENDING) {
            // PENDING: obie strony mogą anulować (najemca lub właściciel narzędzia)
            if (reservation.getRenterId() != userId && tool.ownerId() != userId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(HttpResponse.builder()
                                .statusCode(HttpStatus.FORBIDDEN.value())
                                .httpStatus(HttpStatus.FORBIDDEN)
                                .reason("Forbidden")
                                .message("You don't have permission to cancel this reservation")
                                .build());
            }
        } else if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            // CONFIRMED: obie strony mogą anulować (najemca lub właściciel narzędzia)
            if (reservation.getRenterId() != userId && tool.ownerId() != userId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(HttpResponse.builder()
                                .statusCode(HttpStatus.FORBIDDEN.value())
                                .httpStatus(HttpStatus.FORBIDDEN)
                                .reason("Forbidden")
                                .message("You don't have permission to cancel this reservation")
                                .build());
            }
        }

        reservation = reservationService.cancelReservation(reservationId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(HttpResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .httpStatus(HttpStatus.OK)
                        .reason("Reservation canceled")
                        .message("Reservation canceled")
                        .data(Map.of("reservation", reservationMapper.toDto(reservation)))
                        .build());

    }

    @Operation(summary = "Pobierz szczegóły rezerwacji", description = "Zwraca szczegóły rezerwacji (tylko właściciel lub najemca)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Szczegóły rezerwacji pobrane pomyślnie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "OK",
                                      "statusCode": 200,
                                      "reason": "Reservation details",
                                      "message": "Reservation details",
                                      "data": {
                                        "reservation": {
                                          "id": 1,
                                          "toolId": 1,
                                          "renterId": 2,
                                          "startDate": "2025-12-01",
                                          "endDate": "2025-12-05",
                                          "status": "CONFIRMED"
                                        }
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień - nie jesteś właścicielem ani najemcą",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "FORBIDDEN",
                                      "statusCode": 403,
                                      "reason": "Forbidden",
                                      "message": "You are not the renter or owner of this tool"
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Rezerwacja nie znaleziona",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "NOT_FOUND",
                                      "statusCode": 404,
                                      "reason": "Reservation has not been found",
                                      "message": "Reservation not found"
                                    }
                                    """)))
    })
    @GetMapping("/{reservationId}")
    public ResponseEntity<HttpResponse> getReservationById(
            @PathVariable("reservationId") long reservationId,
            Authentication authentication) {
        final Jwt jwt = (Jwt) authentication.getPrincipal();
        final long userId = jwt.getClaim("user_id");
        Reservation reservation = reservationService.getReservationById(reservationId);

        final ToolExternalDto tool = toolExternalService.getToolDtoById(reservation.getToolId());

        if (reservation.getRenterId() != userId &&
                tool.ownerId() != userId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(HttpResponse.builder()
                            .statusCode(HttpStatus.FORBIDDEN.value())
                            .httpStatus(HttpStatus.FORBIDDEN)
                            .reason("Forbidden")
                            .message("You are not the renter or owner of this tool")
                            .build());
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(HttpResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .httpStatus(HttpStatus.OK)
                        .reason("Reservation details")
                        .message("Reservation details")
                        .data(Map.of("reservation", reservationMapper.toDto(reservation)))
                        .build());

    }

    @Operation(summary = "Pobierz wszystkie rezerwacje", description = "Zwraca stronicowaną listę wszystkich rezerwacji (tylko admin/moderator)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista rezerwacji pobrana pomyślnie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "OK",
                                      "statusCode": 200,
                                      "reason": "All reservations",
                                      "message": "All reservations",
                                      "data": {
                                        "reservations": [],
                                        "totalPages": 5,
                                        "totalItems": 50,
                                        "currentPage": 0
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp - wymagane zalogowanie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "UNAUTHORIZED",
                                      "statusCode": 401,
                                      "reason": "Authorization failed",
                                      "message": "Unauthorized access - authentication required"
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień - wymagana rola ADMIN lub MODERATOR",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "FORBIDDEN",
                                      "statusCode": 403,
                                      "reason": "Access denied",
                                      "message": "Access denied - insufficient permissions"
                                    }
                                    """)))
    })
    @GetMapping("/all")
    public ResponseEntity<HttpResponse> getAllReservations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Reservation> reservations = reservationService.getAllReservations(PageRequest.of(page, size));
        return ResponseEntity.status(HttpStatus.OK)
                .body(HttpResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .httpStatus(HttpStatus.OK)
                        .reason("All reservations")
                        .message("All reservations")
                        .data(Map.of(
                                "reservations", reservations.getContent().stream()
                                        .map(reservationMapper::toDto)
                                        .toList(),
                                "totalPages", reservations.getTotalPages(),
                                "totalItems", reservations.getTotalElements(),
                                "currentPage", reservations.getNumber()
                        ))
                        .build());
    }




}
