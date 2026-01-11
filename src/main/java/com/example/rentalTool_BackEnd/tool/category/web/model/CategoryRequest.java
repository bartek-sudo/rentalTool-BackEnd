package com.example.rentalTool_BackEnd.tool.category.web.model;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Display name is required")
        String displayName,

        String description
) {}
