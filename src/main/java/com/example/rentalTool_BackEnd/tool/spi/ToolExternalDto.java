package com.example.rentalTool_BackEnd.tool.spi;

import com.example.rentalTool_BackEnd.tool.model.enums.Category;

public record ToolExternalDto(
        long id,
        long ownerId,
        double pricePerDay,
        Category category,
        boolean isActive,
        Long termsId
) {
}
