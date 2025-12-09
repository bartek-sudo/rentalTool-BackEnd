package com.example.rentalTool_BackEnd.reservation.service.impl;

import com.example.rentalTool_BackEnd.reservation.model.DailyAvailability;
import com.example.rentalTool_BackEnd.reservation.model.Reservation;
import com.example.rentalTool_BackEnd.reservation.model.enums.ReservationStatus;
import com.example.rentalTool_BackEnd.reservation.repo.ReservationRepo;
import com.example.rentalTool_BackEnd.reservation.service.ToolAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
class ToolAvailabilityServiceImpl implements ToolAvailabilityService {
    private final ReservationRepo reservationRepo;

    @Override
    public List<DailyAvailability> getToolAvailability(long toolId, LocalDate startDate, LocalDate endDate) {
        List<Reservation> activeReservations = reservationRepo.findOverlappingReservations(
                toolId,
                startDate,
                endDate,
                List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED, ReservationStatus.REGULATIONS_ACCEPTED)
        );

        List<LocalDate> allDaysInRange = startDate.datesUntil(endDate.plusDays(1)).toList();

        // Użycie Set dla szybszego wyszukiwania
        Set<LocalDate> reservedDates = new HashSet<>();

        for (Reservation reservation : activeReservations) {
            LocalDate reservationStart = reservation.getStartDate();
            LocalDate reservationEnd = reservation.getEndDate();

            LocalDate overlapStart = reservationStart.isAfter(startDate) ? reservationStart : startDate;
            LocalDate overlapEnd = reservationEnd.isBefore(endDate) ? reservationEnd : endDate;
            reservedDates.addAll(overlapStart.datesUntil(overlapEnd.plusDays(1)).toList());

        }

        return allDaysInRange.stream()
                .map(date -> new DailyAvailability(date, !reservedDates.contains(date)))
                .toList();
    }






}
