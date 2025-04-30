package com.example.rentalTool_BackEnd.tool.web.model;

public record ToolDto(
        String name,
        String description,
        double pricePerDay,
        String category,
        long ownerId,
        String address,
        double latitude,
        double longitude
) {
}
