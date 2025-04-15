package com.example.rentalTool_BackEnd.reservation.repo;

import com.example.rentalTool_BackEnd.reservation.model.Reservation;
import com.example.rentalTool_BackEnd.reservation.model.enums.ReservationStatus;
import com.example.rentalTool_BackEnd.tool.model.Tool;
import com.example.rentalTool_BackEnd.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationJpaRepo extends JpaRepository<Reservation, Long> {

    List<Reservation> findByTool(Tool tool);

    List<Reservation> findByRenter(User renter);

    @Query("SELECT r FROM Reservation r WHERE r.tool.owner = :owner")
    List<Reservation> findByOwner(User owner);

    @Query("SELECT r FROM Reservation r WHERE r.tool.id = :toolId " +
            "AND r.status IN :statuses " +
            "AND ((r.startDate <= :endDate AND r.endDate >= :startDate))")
    List<Reservation> findOverlappingReservations(Long toolId, LocalDate startDate, LocalDate endDate, List<ReservationStatus> statuses);


}
