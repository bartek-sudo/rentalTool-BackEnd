package com.example.rentalTool_BackEnd.tool.terms.service.impl;

import com.example.rentalTool_BackEnd.tool.category.exception.CategoryNotFoundException;
import com.example.rentalTool_BackEnd.tool.category.model.Category;
import com.example.rentalTool_BackEnd.tool.category.repo.CategoryRepo;
import com.example.rentalTool_BackEnd.tool.terms.exception.TermsNotFoundException;
import com.example.rentalTool_BackEnd.tool.terms.model.Terms;
import com.example.rentalTool_BackEnd.tool.terms.repo.TermsRepo;
import com.example.rentalTool_BackEnd.tool.terms.service.TermsService;
import com.example.rentalTool_BackEnd.tool.terms.service.mapper.TermsExternalMapper;
import com.example.rentalTool_BackEnd.tool.spi.TermsExternalDto;
import com.example.rentalTool_BackEnd.tool.spi.TermsExternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class TermsServiceImpl implements TermsService, TermsExternalService {
    private final TermsRepo termsRepo;
    private final TermsExternalMapper termsExternalMapper;
    private final CategoryRepo categoryRepo;

    @Override
    public List<Terms> getTermsForCategory(Category category) {
        return termsRepo.findTermsForCategory(category);
    }

    @Override
    public List<Terms> getAllTerms() {
        return termsRepo.findAllTerms();
    }

    @Override
    public Terms getTermsById(Long id) {
        return termsRepo.findTermsById(id)
                .orElseThrow(() -> new TermsNotFoundException("Terms not found with id: " + id));
    }

    @Override
    public List<Terms> getGeneralTerms() {
        return termsRepo.findGeneralTerms();
    }

    @Override
    public List<Category> getCategoriesWithoutTerms() {
        List<Category> allCategories = categoryRepo.findAll();
        List<Terms> allTerms = termsRepo.findAllTerms();
        
        // Zbierz ID kategorii, które mają już regulaminy
        List<Long> categoriesWithTerms = allTerms.stream()
                .map(terms -> terms.getCategory().getId())
                .collect(Collectors.toList());
        
        // Zwróć kategorie bez regulaminów
        List<Category> categoriesWithoutTerms = allCategories.stream()
                .filter(category -> !categoriesWithTerms.contains(category.getId()))
                .collect(Collectors.toList());
        
        // Zawsze dodaj kategorię OTHER (jeśli nie jest już na liście)
        Category otherCategory = categoryRepo.findByName("OTHER").orElse(null);
        if (otherCategory != null && !categoriesWithoutTerms.contains(otherCategory)) {
            List<Category> result = new ArrayList<>(categoriesWithoutTerms);
            result.add(otherCategory);
            return result;
        }
        
        return categoriesWithoutTerms;
    }

    @Override
    public TermsExternalDto getTermsDtoById(Long id) {
        Terms terms = termsRepo.findTermsById(id)
                .orElseThrow(() -> new TermsNotFoundException("Terms not found with id: " + id));
        return termsExternalMapper.toDto(terms);
    }

    @Override
    public Terms createTerm(Long categoryId, String title, String content) {
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + categoryId));

        // Sprawdź czy kategoria już ma regulamin (z wyjątkiem kategorii OTHER)
        if (!"OTHER".equals(category.getName())) {
            List<Terms> existingTerms = termsRepo.findTermsByCategory(category);
            if (!existingTerms.isEmpty()) {
                throw new IllegalStateException("Category '" + category.getDisplayName() + "' already has a terms document. Each category can have only one terms document.");
            }
        }

        Terms terms = new Terms(category, title, content);
        return termsRepo.saveTerms(terms);
    }

    @Override
    public Terms updateTerm(Long id, Long categoryId, String title, String content) {
        Terms terms = termsRepo.findTermsById(id)
                .orElseThrow(() -> new TermsNotFoundException("Terms not found with id: " + id));

        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + categoryId));

        // Jeśli zmienia się kategoria, sprawdź czy nowa kategoria nie ma już regulaminu
        // (z wyjątkiem kategorii OTHER)
        if (!category.getId().equals(terms.getCategory().getId()) && !"OTHER".equals(category.getName())) {
            List<Terms> existingTerms = termsRepo.findTermsByCategory(category);
            if (!existingTerms.isEmpty()) {
                throw new IllegalStateException("Category '" + category.getDisplayName() + "' already has a terms document. Each category can have only one terms document.");
            }
        }

        terms.setCategory(category);
        terms.setTitle(title);
        terms.setContent(content);

        return termsRepo.saveTerms(terms);
    }

    @Override
    public void deleteTerm(Long id) {
        Terms terms = termsRepo.findTermsById(id)
                .orElseThrow(() -> new TermsNotFoundException("Terms not found with id: " + id));
        termsRepo.deleteTerms(terms);
    }
}
