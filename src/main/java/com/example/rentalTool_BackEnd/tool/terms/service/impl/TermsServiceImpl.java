package com.example.rentalTool_BackEnd.tool.terms.service.impl;

import com.example.rentalTool_BackEnd.tool.terms.exception.TermsNotFoundException;
import com.example.rentalTool_BackEnd.tool.terms.model.Terms;
import com.example.rentalTool_BackEnd.tool.terms.repo.TermsRepo;
import com.example.rentalTool_BackEnd.tool.terms.service.TermsService;
import com.example.rentalTool_BackEnd.tool.terms.service.mapper.TermsExternalMapper;
import com.example.rentalTool_BackEnd.tool.spi.TermsExternalDto;
import com.example.rentalTool_BackEnd.tool.spi.TermsExternalService;
import com.example.rentalTool_BackEnd.shared.enums.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
class TermsServiceImpl implements TermsService, TermsExternalService {
    private final TermsRepo termsRepo;
    private final TermsExternalMapper termsExternalMapper;

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

    // TermsExternalService implementation
    @Override
    public TermsExternalDto getTermsDtoById(Long id) {
        Terms terms = termsRepo.findTermsById(id)
                .orElseThrow(() -> new TermsNotFoundException("Terms not found with id: " + id));
        return termsExternalMapper.toDto(terms);
    }

    // ADMIN methods
    @Override
    public Terms createTerm(String category, String title, String content) {
        Category categoryEnum = null;
        if (category != null && !category.isBlank()) {
            try {
                categoryEnum = Category.valueOf(category.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid category: " + category);
            }
        }

        Terms terms = new Terms(categoryEnum, title, content);
        return termsRepo.saveTerms(terms);
    }

    @Override
    public Terms updateTerm(Long id, String category, String title, String content) {
        Terms terms = termsRepo.findTermsById(id)
                .orElseThrow(() -> new TermsNotFoundException("Terms not found with id: " + id));

        Category categoryEnum = null;
        if (category != null && !category.isBlank()) {
            try {
                categoryEnum = Category.valueOf(category.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid category: " + category);
            }
        }

        terms.setCategory(categoryEnum);
        terms.setTitle(title);
        terms.setContent(content);
        terms.setUpdatedAt(java.time.Instant.now());

        return termsRepo.saveTerms(terms);
    }

    @Override
    public void deleteTerm(Long id) {
        Terms terms = termsRepo.findTermsById(id)
                .orElseThrow(() -> new TermsNotFoundException("Terms not found with id: " + id));
        termsRepo.deleteTerms(terms);
    }
}
