package com.example.rentalTool_BackEnd.reservation.service.impl;

import com.example.rentalTool_BackEnd.reservation.exception.ReservationNotFoundException;
import com.example.rentalTool_BackEnd.reservation.exception.ToolNotAvailableException;
import com.example.rentalTool_BackEnd.reservation.model.Reservation;
import com.example.rentalTool_BackEnd.reservation.model.enums.ReservationStatus;
import com.example.rentalTool_BackEnd.reservation.repo.ReservationRepo;
import com.example.rentalTool_BackEnd.reservation.service.ReservationService;
import com.example.rentalTool_BackEnd.tool.model.Tool;
import com.example.rentalTool_BackEnd.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepo reservationRepo;

    @Override
    public boolean isToolAvailable(Tool tool, LocalDate startDate, LocalDate endDate) {
        List<Reservation> overlappingReservations = reservationRepo.findOverlappingReservations(
                tool.getId(),
                startDate,
                endDate,
                List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED, ReservationStatus.PAID)
        );
        return overlappingReservations.isEmpty();
    }

    @Override
    public Reservation createReservation(Tool tool, User renter, LocalDate startDate, LocalDate endDate) {
        if (!isToolAvailable(tool, startDate, endDate)) {
            throw new ToolNotAvailableException("Tool is not available for the selected dates");
        }

        final Reservation reservation = new Reservation(tool, renter, startDate, endDate);
        return reservationRepo.saveReservation(reservation);
    }

    @Override
    public Reservation getReservationById(long id) {
        return reservationRepo.findReservationById(id)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));
    }

    @Override
    public List<Reservation> getReservationsForTool(Tool tool) {
        return reservationRepo.findReservationByTool(tool);
    }

    @Override
    public List<Reservation> getReservationsForRenter(User renter) {
        return reservationRepo.findReservationByRenter(renter);
    }

    @Override
    public List<Reservation> getReservationsForOwner(User owner) {
        return reservationRepo.findReservationsByToolOwner(owner);
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
