package com.example.rentalTool_BackEnd.tool.spi;

public record TermsExternalDto(
        Long id,
        Long categoryId,
        String categoryName,
        String title,
        String content
) {
}
