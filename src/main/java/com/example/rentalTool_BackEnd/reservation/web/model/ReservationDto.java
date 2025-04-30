package com.example.rentalTool_BackEnd.reservation.web.model;

public record ReservationDto(
        long toolId,
        long renterId,
        String startDate,
        String endDate,
        double totalPrice,
        String status
) {
}
