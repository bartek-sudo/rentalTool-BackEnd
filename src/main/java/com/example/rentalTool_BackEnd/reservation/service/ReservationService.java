package com.example.rentalTool_BackEnd.reservation.service;

import com.example.rentalTool_BackEnd.reservation.model.Reservation;

import java.time.LocalDate;
import java.util.List;

public interface ReservationService {

    boolean isToolAvailable(long toolId, LocalDate startDate, LocalDate endDate);

    Reservation createReservation(long toolId, long renterId, LocalDate startDate, LocalDate endDate);

    Reservation getReservationById(long id);

    List<Reservation> getReservationsForRenter(long renterId);

    List<Reservation> getReservationsForOwner(long ownerId);

    Reservation confirmReservation(long reservationId);

    Reservation payReservation(long reservationId);

    Reservation finishReservation(long reservationId);

    Reservation cancelReservation(long reservationId);
}
