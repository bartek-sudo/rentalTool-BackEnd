package com.example.rentalTool_BackEnd.reservation.web.controller;

import com.example.rentalTool_BackEnd.reservation.model.DailyAvailability;
import com.example.rentalTool_BackEnd.reservation.service.ToolAvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "Sprawdź dostępność narzędzia", description = "Zwraca informacje o dostępności narzędzia w zadanym zakresie dat (maksymalnie 3 miesiące)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dostępność narzędzia pobrana pomyślnie",
                    content = @Content(schema = @Schema(implementation = DailyAvailability.class),
                            examples = @ExampleObject(value = """
                                    [
                                      {
                                        "date": "2025-12-01",
                                        "available": true
                                      },
                                      {
                                        "date": "2025-12-02",
                                        "available": false
                                      },
                                      {
                                        "date": "2025-12-03",
                                        "available": true
                                      }
                                    ]
                                    """))),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowy zakres dat",
                    content = @Content(schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "Invalid date range"))),
            @ApiResponse(responseCode = "404", description = "Narzędzie nie znalezione",
                    content = @Content(schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "Tool not found")))
    })
    @GetMapping("/{toolId}/availability")
    public ResponseEntity<List<DailyAvailability>> getToolAvailability(
            @Parameter(description = "ID narzędzia", example = "1")
            @PathVariable("toolId") long toolId,
            @Parameter(description = "Data początkowa (format: YYYY-MM-DD)", example = "2025-12-01")
            @RequestParam("startDate") LocalDate startDate,
            @Parameter(description = "Data końcowa (format: YYYY-MM-DD, maksymalnie 3 miesiące od daty początkowej)", example = "2025-12-31")
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
