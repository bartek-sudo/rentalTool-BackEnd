package com.example.rentalTool_BackEnd.reservation.web.model;

public record ReservationDto(
        String toolName,
        String renterEmail,
        String startDate,
        String endDate,
        double totalPrice,
        String status
) {
}
