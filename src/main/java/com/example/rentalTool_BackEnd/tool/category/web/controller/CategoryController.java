package com.example.rentalTool_BackEnd.tool.category.web.controller;

import com.example.rentalTool_BackEnd.shared.model.HttpResponse;
import com.example.rentalTool_BackEnd.tool.category.model.Category;
import com.example.rentalTool_BackEnd.tool.category.service.CategoryService;
import com.example.rentalTool_BackEnd.tool.category.web.mapper.CategoryMapper;
import com.example.rentalTool_BackEnd.tool.category.web.model.CategoryDto;
import com.example.rentalTool_BackEnd.tool.category.web.model.CategoryRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @GetMapping
    public ResponseEntity<HttpResponse> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        List<CategoryDto> categoryDtos = categories.stream()
                .map(categoryMapper::toDto)
                .toList();

        return ResponseEntity.status(HttpStatus.OK)
                .body(HttpResponse.builder()
                        .data(Map.of("categories", categoryDtos))
                        .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HttpResponse> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        CategoryDto categoryDto = categoryMapper.toDto(category);

        return ResponseEntity.status(HttpStatus.OK)
                .body(HttpResponse.builder()
                        .data(Map.of("category", categoryDto))
                        .build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<HttpResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        Category category = categoryService.createCategory(
                request.name(),
                request.displayName(),
                request.description()
        );
        CategoryDto categoryDto = categoryMapper.toDto(category);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(HttpResponse.builder()
                        .data(Map.of("category", categoryDto))
                        .message("Category created successfully")
                        .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<HttpResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        Category category = categoryService.updateCategory(
                id,
                request.displayName(),
                request.description()
        );
        CategoryDto categoryDto = categoryMapper.toDto(category);

        return ResponseEntity.status(HttpStatus.OK)
                .body(HttpResponse.builder()
                        .data(Map.of("category", categoryDto))
                        .message("Category updated successfully")
                        .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<HttpResponse> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);

        return ResponseEntity.status(HttpStatus.OK)
                .body(HttpResponse.builder()
                        .message("Category deleted successfully")
                        .build());
    }
}
