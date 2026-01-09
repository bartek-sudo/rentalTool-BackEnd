package com.example.rentalTool_BackEnd.tool.terms.web.model;

public record TermsDto(
        Long id,
        String category,
        String title,
        String content
) {
}
