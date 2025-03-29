package com.example.rentalTool_BackEnd.tool.web.model;

public record ToolDto(
        String name,
        String description,
        double pricePerDay,
        String category,
        String ownerEmail,
        String address,
        double latitude,
        double longitude
) {
}
