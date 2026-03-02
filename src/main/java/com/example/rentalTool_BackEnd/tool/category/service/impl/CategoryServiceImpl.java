package com.example.rentalTool_BackEnd.tool.category.service.impl;

import com.example.rentalTool_BackEnd.tool.category.exception.CategoryNotFoundException;
import com.example.rentalTool_BackEnd.tool.category.exception.DuplicateCategoryException;
import com.example.rentalTool_BackEnd.tool.category.model.Category;
import com.example.rentalTool_BackEnd.tool.category.repo.CategoryRepo;
import com.example.rentalTool_BackEnd.tool.category.service.CategoryService;
import com.example.rentalTool_BackEnd.tool.terms.model.Terms;
import com.example.rentalTool_BackEnd.tool.terms.repo.TermsRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
class CategoryServiceImpl implements CategoryService {
    private final CategoryRepo categoryRepo;
    private final TermsRepo termsRepo;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Category> getAllCategories() {
        return categoryRepo.findAll();
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryRepo.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + id));
    }

    @Override
    public Category getCategoryByName(String name) {
        return categoryRepo.findByName(name.toUpperCase())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with name: " + name));
    }

    @Override
    public Category createCategory(String name, String displayName, String description) {
        String normalizedName = name.trim().toUpperCase();

        if (categoryRepo.existsByName(normalizedName)) {
            throw new DuplicateCategoryException("Category with name '" + normalizedName + "' already exists");
        }

        Category category = new Category(normalizedName, displayName, description);
        return categoryRepo.save(category);
    }

    @Override
    public Category updateCategory(Long id, String displayName, String description) {
        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + id));

        category.setDisplayName(displayName);
        category.setDescription(description);

        return categoryRepo.save(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + id));

        // Zabezpieczenie przed usunięciem kategorii OTHER (używana jako domyślna)
        if ("OTHER".equals(category.getName())) {
            throw new IllegalStateException("Cannot delete default category 'OTHER'");
        }

        // Znajdź kategorię "OTHER" jako domyślną dla przeniesienia narzędzi
        Category otherCategory = categoryRepo.findByName("OTHER")
                .orElseThrow(() -> new IllegalStateException("Default category 'OTHER' not found in database"));

        // Znajdź regulamin dla kategorii OTHER (weź pierwszy, jeśli jest wiele)
        List<Terms> otherTerms = termsRepo.findTermsByCategory(otherCategory);
        Long otherTermsId = null;
        if (!otherTerms.isEmpty()) {
            otherTermsId = otherTerms.get(0).getId();
        }

        // Przenieś wszystkie narzędzia z usuwanej kategorii do kategorii "OTHER"
        // Używamy bezpośrednio SQL dla wydajności i uniknięcia problemów z dostępem do repozytoriów
        if (otherTermsId != null) {
            // Aktualizuj zarówno category_id jak i terms_id
            jdbcTemplate.update(
                    "UPDATE tools SET category_id = ?, terms_id = ? WHERE category_id = ?",
                    otherCategory.getId(),
                    otherTermsId,
                    id
            );
        } else {
            // Jeśli nie ma regulaminu dla OTHER, tylko przenieś kategorię
            jdbcTemplate.update(
                    "UPDATE tools SET category_id = ? WHERE category_id = ?",
                    otherCategory.getId(),
                    id
            );
        }

        // Usuń wszystkie regulaminy powiązane z tą kategorią
        List<Terms> relatedTerms = termsRepo.findTermsByCategory(category);
        relatedTerms.forEach(termsRepo::deleteTerms);

        categoryRepo.delete(category);
    }
}
