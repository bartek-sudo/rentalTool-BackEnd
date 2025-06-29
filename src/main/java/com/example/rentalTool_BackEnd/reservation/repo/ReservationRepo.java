package com.example.rentalTool_BackEnd.reservation.repo;

import com.example.rentalTool_BackEnd.reservation.model.Reservation;
import com.example.rentalTool_BackEnd.reservation.model.enums.ReservationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReservationRepo {
    private final ReservationJpaRepo reservationJpaRepo;

    public Reservation saveReservation(Reservation reservation) {
        return reservationJpaRepo.save(reservation);
    }

    public List<Reservation> findReservationByRenterId(long renterId) {
        return reservationJpaRepo.findByRenterId(renterId);
    }

    public List<Reservation> findByToolId(long toolId) {
        return reservationJpaRepo.findByToolId(toolId);
    }

    public List<Reservation> findOverlappingReservations(
            @Param("toolId") long toolId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statuses") List<ReservationStatus> statuses) {
        return reservationJpaRepo.findOverlappingReservations(toolId, startDate, endDate, statuses);
    }


    public Optional<Reservation> findReservationById(long id) {
        return reservationJpaRepo.findById(id);
    }

    public Page<Reservation> findAll(Pageable pageable) {
        return reservationJpaRepo.findAll(pageable);
    }
}
