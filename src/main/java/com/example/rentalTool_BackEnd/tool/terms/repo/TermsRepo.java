package com.example.rentalTool_BackEnd.tool.terms.repo;

import com.example.rentalTool_BackEnd.tool.category.model.Category;
import com.example.rentalTool_BackEnd.tool.terms.model.Terms;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TermsRepo {
    private final TermsJpaRepo termsJpaRepo;

    public Terms saveTerms(Terms terms) {
        return termsJpaRepo.save(terms);
    }

    public Optional<Terms> findTermsById(Long id) {
        return termsJpaRepo.findById(id);
    }

    public List<Terms> findTermsByCategory(Category category) {
        return termsJpaRepo.findByCategory(category);
    }

    public List<Terms> findGeneralTerms() {
        return termsJpaRepo.findGeneralTerms();
    }

    public List<Terms> findTermsForCategory(Category category) {
        return termsJpaRepo.findTermsForCategory(category);
    }

    public List<Terms> findAllTerms() {
        return termsJpaRepo.findAll();
    }

    public void deleteTerms(Terms terms) {
        termsJpaRepo.delete(terms);
    }
}
