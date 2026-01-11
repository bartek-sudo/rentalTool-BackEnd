package com.example.rentalTool_BackEnd.tool.terms.repo;

import com.example.rentalTool_BackEnd.tool.category.model.Category;
import com.example.rentalTool_BackEnd.tool.terms.model.Terms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TermsJpaRepo extends JpaRepository<Terms, Long> {

    List<Terms> findByCategory(Category category);

    @Query("SELECT t FROM Terms t WHERE t.category IS NULL")
    List<Terms> findGeneralTerms();

    @Query("SELECT t FROM Terms t WHERE t.category = :category OR t.category IS NULL ORDER BY t.category.name NULLS LAST")
    List<Terms> findTermsForCategory(@Param("category") Category category);

    Optional<Terms> findById(Long id);
}
