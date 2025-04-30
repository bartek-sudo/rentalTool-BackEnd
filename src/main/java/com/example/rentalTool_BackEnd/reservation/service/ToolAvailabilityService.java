package com.example.rentalTool_BackEnd.reservation.service;

import com.example.rentalTool_BackEnd.reservation.model.DailyAvailability;
import java.time.LocalDate;
import java.util.List;

public interface ToolAvailabilityService {
    List<DailyAvailability> getToolAvailability(long toolId, LocalDate startDate, LocalDate endDate);
}
