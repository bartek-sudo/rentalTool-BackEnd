package com.example.rentalTool_BackEnd.reservation.service;

import com.example.rentalTool_BackEnd.reservation.model.Reservation;
import com.example.rentalTool_BackEnd.tool.model.Tool;
import com.example.rentalTool_BackEnd.user.model.User;

import java.time.LocalDate;
import java.util.List;

public interface ReservationService {

    boolean isToolAvailable(Tool tool, LocalDate startDate, LocalDate endDate);

    Reservation createReservation(Tool tool, User renter, LocalDate startDate, LocalDate endDate);

    Reservation getReservationById(long id);

    List<Reservation> getReservationsForTool(Tool tool);

    List<Reservation> getReservationsForRenter(User renter);

    List<Reservation> getReservationsForOwner(User owner);

    Reservation confirmReservation(long reservationId);

    Reservation payReservation(long reservationId);

    Reservation finishReservation(long reservationId);

    Reservation cancelReservation(long reservationId);
}
