package com.example.rentalTool_BackEnd.tool.terms.service.mapper;

import com.example.rentalTool_BackEnd.tool.terms.model.Terms;
import com.example.rentalTool_BackEnd.tool.spi.TermsExternalDto;
import org.springframework.stereotype.Component;

@Component
public class TermsExternalMapper {
    public TermsExternalDto toDto(Terms terms) {
        return new TermsExternalDto(
                terms.getId(),
                terms.getCategory() != null ? terms.getCategory().name() : null,
                terms.getTitle(),
                terms.getContent()
        );
    }
}
