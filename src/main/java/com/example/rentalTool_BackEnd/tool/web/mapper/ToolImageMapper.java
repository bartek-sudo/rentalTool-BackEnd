package com.example.rentalTool_BackEnd.tool.web.mapper;

import com.example.rentalTool_BackEnd.tool.model.ToolImage;
import com.example.rentalTool_BackEnd.tool.web.model.ToolImageDto;
import org.springframework.stereotype.Component;

@Component
public class ToolImageMapper {
    public ToolImageDto toDto(ToolImage toolImage) {
        return new ToolImageDto(
                toolImage.getId(),
                toolImage.getUrl(),
                toolImage.getFilename(),
                toolImage.getContentType(),
                toolImage.isMain(),
                toolImage.getCreatedAt(),
                toolImage.getUpdatedAt()
        );
    }
}
