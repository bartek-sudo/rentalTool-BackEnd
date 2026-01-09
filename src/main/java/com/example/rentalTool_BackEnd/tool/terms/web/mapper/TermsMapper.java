package com.example.rentalTool_BackEnd.tool.terms.web.mapper;

import com.example.rentalTool_BackEnd.tool.terms.model.Terms;
import com.example.rentalTool_BackEnd.tool.terms.web.model.TermsDto;
import org.springframework.stereotype.Component;

@Component
public class TermsMapper {
    public TermsDto toDto(Terms terms) {
        return new TermsDto(
                terms.getId(),
                terms.getCategory() != null ? terms.getCategory().name() : null,
                terms.getTitle(),
                terms.getContent()
        );
    }
}
