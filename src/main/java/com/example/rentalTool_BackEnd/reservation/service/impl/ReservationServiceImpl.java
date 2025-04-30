package com.example.rentalTool_BackEnd.reservation.service.impl;

import com.example.rentalTool_BackEnd.reservation.exception.ReservationNotFoundException;
import com.example.rentalTool_BackEnd.reservation.exception.ToolNotAvailableException;
import com.example.rentalTool_BackEnd.reservation.model.Reservation;
import com.example.rentalTool_BackEnd.reservation.model.enums.ReservationStatus;
import com.example.rentalTool_BackEnd.reservation.repo.ReservationRepo;
import com.example.rentalTool_BackEnd.reservation.service.ReservationService;
import com.example.rentalTool_BackEnd.tool.spi.ToolExternalDto;
import com.example.rentalTool_BackEnd.tool.spi.ToolExternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
class ReservationServiceImpl implements ReservationService {

    private final ReservationRepo reservationRepo;
    private final ToolExternalService toolExternalService;

    @Override
    public boolean isToolAvailable(long toolId, LocalDate startDate, LocalDate endDate) {
        List<Reservation> overlappingReservations = reservationRepo.findOverlappingReservations(
                toolId,
                startDate,
                endDate,
                List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED, ReservationStatus.PAID)
        );
        return overlappingReservations.isEmpty();
    }

    @Override
    public Reservation createReservation(long toolId, long renterId, LocalDate startDate, LocalDate endDate) {
        if (!isToolAvailable(toolId, startDate, endDate)) {
            throw new ToolNotAvailableException("Tool is not available for the selected dates");
        }

        final ToolExternalDto tool = toolExternalService.getToolDtoById(toolId);

        final Reservation reservation = new Reservation(toolId, renterId, startDate, endDate);

        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        double totalPrice = tool.pricePerDay() * days;
        reservation.setTotalPrice(totalPrice);

        return reservationRepo.saveReservation(reservation);
    }

    @Override
    public Reservation getReservationById(long id) {
        return reservationRepo.findReservationById(id)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));
    }

    @Override
    public List<Reservation> getReservationsForRenter(long renterId) {
        return reservationRepo.findReservationByRenterId(renterId);
    }

    @Override
    public List<Reservation> getReservationsForOwner(long ownerId) {
        // Krok 1: Pobierz wszystkie narzędzia należące do danego użytkownika
        List<ToolExternalDto> ownerTools = toolExternalService.getToolsByOwnerId(ownerId);

        // Krok 2: Pobierz wszystkie rezerwacje dla tych narzędzi
        List<Reservation> allReservations = new ArrayList<>();
        for (ToolExternalDto tool : ownerTools) {
            List<Reservation> toolReservations = reservationRepo.findByToolId(tool.id());
            allReservations.addAll(toolReservations);
        }

        return allReservations;
    }

    @Override
    public Reservation confirmReservation(long reservationId) {
        Reservation reservation = getReservationById(reservationId);
        reservation.confirm();
        return reservationRepo.saveReservation(reservation);
    }

    @Override
    public Reservation payReservation(long reservationId) {
        Reservation reservation = getReservationById(reservationId);
        reservation.pay();
        return reservationRepo.saveReservation(reservation);
    }

    @Override
    public Reservation finishReservation(long reservationId) {
        Reservation reservation = getReservationById(reservationId);
        reservation.finish();
        return reservationRepo.saveReservation(reservation);
    }

    @Override
    public Reservation cancelReservation(long reservationId) {
        Reservation reservation = getReservationById(reservationId);
        reservation.cancel();
        return reservationRepo.saveReservation(reservation);
    }




}
