package com.example.rentalTool_BackEnd.reservation.service;

import com.example.rentalTool_BackEnd.reservation.model.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ReservationService {

    boolean isToolAvailable(long toolId, LocalDate startDate, LocalDate endDate);

    Reservation createReservation(long toolId, long renterId, LocalDate startDate, LocalDate endDate);

    Reservation getReservationById(long id);

    List<Reservation> getReservationsForRenter(long renterId);

    List<Reservation> getReservationsForOwner(long ownerId);

    Reservation confirmReservation(long reservationId);

    Reservation acceptRegulationsReservation(long reservationId, Long termsId);

    Reservation cancelReservation(long reservationId);

    Page<Reservation> getAllReservations(Pageable pageable);
}
