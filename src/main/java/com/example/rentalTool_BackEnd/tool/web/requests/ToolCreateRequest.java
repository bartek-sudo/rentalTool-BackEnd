package com.example.rentalTool_BackEnd.tool.web.requests;

import java.math.BigDecimal;

public record ToolCreateRequest(
        String name,
        String description,
        double pricePerDay,
        String category,
        String address,
        Double latitude,
        Double longitude

//        String images
) {
}
