package com.example.rentalTool_BackEnd.reservation.web.mapper;

import com.example.rentalTool_BackEnd.reservation.model.Reservation;
import com.example.rentalTool_BackEnd.reservation.web.model.ReservationDto;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {
    public ReservationDto toDto(Reservation reservation) {
        return new ReservationDto(
                reservation.getId(),
                reservation.getToolId(),
                reservation.getRenterId(),
                reservation.getStartDate().toString(),
                reservation.getEndDate().toString(),
                reservation.getTotalPrice(),
                reservation.getStatus().name(),
                reservation.getTermsId()
        );
    }
}
