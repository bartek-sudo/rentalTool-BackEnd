package com.example.rentalTool_BackEnd.tool.terms.web.model;

public record TermsDto(
        Long id,
        Long categoryId,
        String categoryName,
        String title,
        String content
) {
}
