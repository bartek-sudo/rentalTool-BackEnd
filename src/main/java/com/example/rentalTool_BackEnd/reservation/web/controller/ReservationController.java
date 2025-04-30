package com.example.rentalTool_BackEnd.reservation.web.controller;

import com.example.rentalTool_BackEnd.reservation.model.Reservation;
import com.example.rentalTool_BackEnd.reservation.service.ReservationService;
import com.example.rentalTool_BackEnd.reservation.web.mapper.ReservationMapper;
import com.example.rentalTool_BackEnd.reservation.web.requests.ReservationCreateRequest;
import com.example.rentalTool_BackEnd.shared.model.HttpResponse;
import com.example.rentalTool_BackEnd.tool.spi.ToolExternalDto;
import com.example.rentalTool_BackEnd.tool.spi.ToolExternalService;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/create")
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

    @PutMapping("/{reservationId}/pay")
    public ResponseEntity<HttpResponse> payReservation(
            @PathVariable("reservationId") long reservationId,
            Authentication authentication) {
        final Jwt jwt = (Jwt) authentication.getPrincipal();
        final long userId = jwt.getClaim("user_id");
        Reservation reservation = reservationService.getReservationById(reservationId);

        if (reservation.getRenterId() != userId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(HttpResponse.builder()
                            .statusCode(HttpStatus.FORBIDDEN.value())
                            .httpStatus(HttpStatus.FORBIDDEN)
                            .reason("Forbidden")
                            .message("You are not the renter of this tool")
                            .build());
        }

        reservation = reservationService.payReservation(reservationId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(HttpResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .httpStatus(HttpStatus.OK)
                        .reason("Reservation paid")
                        .message("Reservation paid")
                        .data(Map.of("reservation", reservationMapper.toDto(reservation)))
                        .build());

    }

    @PutMapping("/{reservationId}/finish")
    public ResponseEntity<HttpResponse> finishReservation(
            @PathVariable("reservationId") long reservationId,
            Authentication authentication) {
        final Jwt jwt = (Jwt) authentication.getPrincipal();
        final long userId = jwt.getClaim("user_id");
        Reservation reservation = reservationService.getReservationById(reservationId);

        if (reservation.getRenterId() != userId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(HttpResponse.builder()
                            .statusCode(HttpStatus.FORBIDDEN.value())
                            .httpStatus(HttpStatus.FORBIDDEN)
                            .reason("Forbidden")
                            .message("You are not the renter of this tool")
                            .build());
        }

        reservation = reservationService.finishReservation(reservationId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(HttpResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .httpStatus(HttpStatus.OK)
                        .reason("Reservation finished")
                        .message("Reservation finished")
                        .data(Map.of("reservation", reservationMapper.toDto(reservation)))
                        .build());

    }

    @PutMapping("/{reservationId}/cancel")
    public ResponseEntity<HttpResponse> cancelReservation(
            @PathVariable("reservationId") long reservationId,
            Authentication authentication) {
        final Jwt jwt = (Jwt) authentication.getPrincipal();
        final long userId = jwt.getClaim("user_id");
        Reservation reservation = reservationService.getReservationById(reservationId);

        if (reservation.getRenterId() != userId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(HttpResponse.builder()
                            .statusCode(HttpStatus.FORBIDDEN.value())
                            .httpStatus(HttpStatus.FORBIDDEN)
                            .reason("Forbidden")
                            .message("You are not the renter of this tool")
                            .build());
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






}
