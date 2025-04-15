package com.example.rentalTool_BackEnd.reservation.web.requests;

import java.time.LocalDate;

public record ReservationCreateRequest(
        long toolId,
        LocalDate startDate,
        LocalDate endDate
) {
}
