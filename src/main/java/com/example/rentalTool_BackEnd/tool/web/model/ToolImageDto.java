package com.example.rentalTool_BackEnd.tool.web.model;

import java.time.Instant;

public record ToolImageDto(
        long id,
        String url,
        String filename,
        String contentType,
        boolean isMain,
        Instant createdAt,
        Instant updatedAt
) {
}
