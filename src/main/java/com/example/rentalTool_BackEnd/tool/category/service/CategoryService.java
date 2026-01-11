package com.example.rentalTool_BackEnd.tool.category.service;

import com.example.rentalTool_BackEnd.tool.category.model.Category;

import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();
    Category getCategoryById(Long id);
    Category getCategoryByName(String name);

    // ADMIN methods
    Category createCategory(String name, String displayName, String description);
    Category updateCategory(Long id, String displayName, String description);
    void deleteCategory(Long id);
}
