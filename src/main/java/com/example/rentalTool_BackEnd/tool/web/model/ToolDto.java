package com.example.rentalTool_BackEnd.tool.web.model;

public record ToolDto(
        long id,
        String name,
        String description,
        double pricePerDay,
        String category,
        long ownerId,
        String address,
        double latitude,
        double longitude,
        String mainImageUrl,
        boolean isActive,
        String moderationStatus,
        String createdAt,
        String moderationComment
) {
}
