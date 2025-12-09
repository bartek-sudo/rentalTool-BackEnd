package com.example.rentalTool_BackEnd.reservation.repo;

import com.example.rentalTool_BackEnd.reservation.model.Reservation;
import com.example.rentalTool_BackEnd.reservation.model.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationJpaRepo extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r WHERE r.renterId = :renterId ORDER BY r.createdAt DESC")
    List<Reservation> findByRenterId(long renterId);

    @Query("SELECT r FROM Reservation r WHERE r.toolId = :toolId ORDER BY r.createdAt DESC")
    List<Reservation> findByToolId(long toolId);

    @Query("SELECT r FROM Reservation r WHERE r.toolId = :toolId " +
            "AND r.status IN :statuses " +
            "AND ((r.startDate <= :endDate AND r.endDate >= :startDate))")
    List<Reservation> findOverlappingReservations(Long toolId, LocalDate startDate, LocalDate endDate, List<ReservationStatus> statuses);


}
