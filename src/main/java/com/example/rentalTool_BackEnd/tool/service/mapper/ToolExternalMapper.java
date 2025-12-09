package com.example.rentalTool_BackEnd.tool.service.mapper;

import com.example.rentalTool_BackEnd.tool.model.Tool;
import com.example.rentalTool_BackEnd.tool.spi.ToolExternalDto;
import org.springframework.stereotype.Component;

@Component
public class ToolExternalMapper {
    public ToolExternalDto toDto (Tool tool) {
        return new ToolExternalDto(
                tool.getId(),
                tool.getOwnerId(),
                tool.getPricePerDay(),
                tool.getCategory(),
                tool.isActive(),
                tool.getTermsId()
        );
    }
}
