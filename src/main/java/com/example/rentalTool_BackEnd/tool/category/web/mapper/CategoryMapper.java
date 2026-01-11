package com.example.rentalTool_BackEnd.tool.category.web.mapper;

import com.example.rentalTool_BackEnd.tool.category.model.Category;
import com.example.rentalTool_BackEnd.tool.category.web.model.CategoryDto;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    public CategoryDto toDto(Category category) {
        return new CategoryDto(
                category.getId(),
                category.getName(),
                category.getDisplayName(),
                category.getDescription()
        );
    }
}
