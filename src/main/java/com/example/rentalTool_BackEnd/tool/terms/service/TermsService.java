package com.example.rentalTool_BackEnd.tool.terms.service;

import com.example.rentalTool_BackEnd.tool.category.model.Category;
import com.example.rentalTool_BackEnd.tool.terms.model.Terms;

import java.util.List;

public interface TermsService {
    List<Terms> getTermsForCategory(Category category);

    List<Terms> getAllTerms();

    Terms getTermsById(Long id);

    List<Terms> getGeneralTerms();

    List<Category> getCategoriesWithoutTerms();

    Terms createTerm(Long categoryId, String title, String content);

    Terms updateTerm(Long id, Long categoryId, String title, String content);

    void deleteTerm(Long id);
}
