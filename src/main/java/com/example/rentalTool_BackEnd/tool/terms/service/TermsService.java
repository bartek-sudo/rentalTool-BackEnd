package com.example.rentalTool_BackEnd.tool.terms.service;

import com.example.rentalTool_BackEnd.tool.terms.model.Terms;
import com.example.rentalTool_BackEnd.shared.enums.Category;

import java.util.List;

public interface TermsService {
    List<Terms> getTermsForCategory(Category category);

    List<Terms> getAllTerms();

    Terms getTermsById(Long id);

    List<Terms> getGeneralTerms();

    // ADMIN methods
    Terms createTerm(String category, String title, String content);

    Terms updateTerm(Long id, String category, String title, String content);

    void deleteTerm(Long id);
}
