package com.example.rentalTool_BackEnd.tool.terms.web.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TermsRequest(
        @NotNull(message = "Category ID is required")
        Long categoryId,

        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must not exceed 200 characters")
        String title,

        @NotBlank(message = "Content is required")
        String content
) {
}
