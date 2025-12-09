package com.example.rentalTool_BackEnd.tool.web.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record ToolUpdateRequest(
        @NotBlank
        String name,
        String description,
        double pricePerDay,
        String category,
        String address,
        @NotNull
        Double latitude,
        @NotNull
        Double longitude,
        @NotNull
        Long termsId

) {
}
