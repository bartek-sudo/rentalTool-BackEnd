package com.example.rentalTool_BackEnd.reservation.web.controller;

import com.example.rentalTool_BackEnd.reservation.model.DailyAvailability;
import com.example.rentalTool_BackEnd.reservation.service.ToolAvailabilityService;
import com.example.rentalTool_BackEnd.tool.spi.ToolExternalDto;
import com.example.rentalTool_BackEnd.tool.spi.ToolExternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tools")
@RequiredArgsConstructor
public class ToolAvailabilityController {

    private final ToolAvailabilityService toolAvailabilityService;

    @GetMapping("/{toolId}/availability")
    public ResponseEntity<List<DailyAvailability>> getToolAvailability(
            @PathVariable("toolId") long toolId,
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate
    ) {

        LocalDate maxEndDate = startDate.plusMonths(3);
        if (endDate.isAfter(maxEndDate)) {
            endDate = maxEndDate;
        }

        List<DailyAvailability> availability = toolAvailabilityService.getToolAvailability(toolId, startDate, endDate);
        return ResponseEntity.ok(availability);
    }




}
