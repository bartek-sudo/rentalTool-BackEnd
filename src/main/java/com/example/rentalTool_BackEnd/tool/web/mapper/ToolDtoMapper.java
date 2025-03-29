package com.example.rentalTool_BackEnd.tool.web.mapper;

import com.example.rentalTool_BackEnd.tool.model.Tool;
import com.example.rentalTool_BackEnd.tool.web.model.ToolDto;
import org.springframework.stereotype.Component;

@Component
public class ToolDtoMapper {
    public ToolDto toDto(Tool tool) {
        return new ToolDto(
                tool.getName(),
                tool.getDescription(),
                tool.getPricePerDay(),
                tool.getCategory().name(),
                tool.getOwner().getEmail(),
                tool.getAddress(),
                tool.getLatitude(),
                tool.getLongitude()
        );
    }
}
