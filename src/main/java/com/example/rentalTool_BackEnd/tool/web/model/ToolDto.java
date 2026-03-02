package com.example.rentalTool_BackEnd.tool.web.model;

public record ToolDto(
        long id,
        String name,
        String description,
        double pricePerDay,
        String category,
        String displayName,
        long ownerId,
        String address,
        double latitude,
        double longitude,
        String mainImageUrl,
        Long termsId,
        boolean isActive,
        String moderationStatus,
        String createdAt,
        String moderationComment,
        Double distance // Odległość od użytkownika w km (null jeśli nie wyszukiwano po lokalizacji)
) {
}
