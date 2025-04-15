package com.example.rentalTool_BackEnd.reservation.repo;

import com.example.rentalTool_BackEnd.reservation.model.Reservation;
import com.example.rentalTool_BackEnd.reservation.model.enums.ReservationStatus;
import com.example.rentalTool_BackEnd.tool.model.Tool;
import com.example.rentalTool_BackEnd.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Query;
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

    public List<Reservation> findReservationByTool(Tool tool) {
        return reservationJpaRepo.findByTool(tool);
    }

    public List<Reservation> findReservationByRenter(User renter) {
        return reservationJpaRepo.findByRenter(renter);
    }

    public List<Reservation> findReservationsByToolOwner(User owner) {
        return reservationJpaRepo.findByOwner(owner);
    }

    public List<Reservation> findOverlappingReservations(
            @Param("toolId") Long toolId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statuses") List<ReservationStatus> statuses) {
        return reservationJpaRepo.findOverlappingReservations(toolId, startDate, endDate, statuses);
    }


    public Optional<Reservation> findReservationById(long id) {
        return reservationJpaRepo.findById(id);
    }
}
