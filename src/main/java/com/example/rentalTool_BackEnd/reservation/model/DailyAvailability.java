package com.example.rentalTool_BackEnd.reservation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyAvailability {
    private LocalDate date;
    private boolean available;
}
