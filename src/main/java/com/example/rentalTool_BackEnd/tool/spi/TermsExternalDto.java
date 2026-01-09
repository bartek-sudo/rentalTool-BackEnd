package com.example.rentalTool_BackEnd.tool.spi;

public record TermsExternalDto(
        Long id,
        String category,
        String title,
        String content
) {
}
