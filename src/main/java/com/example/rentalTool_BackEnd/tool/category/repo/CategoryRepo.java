package com.example.rentalTool_BackEnd.tool.category.repo;

import com.example.rentalTool_BackEnd.tool.category.model.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CategoryRepo {
    private final CategoryJpaRepo categoryJpaRepo;

    public Category save(Category category) {
        return categoryJpaRepo.save(category);
    }

    public Optional<Category> findById(Long id) {
        return categoryJpaRepo.findById(id);
    }

    public Optional<Category> findByName(String name) {
        return categoryJpaRepo.findByName(name);
    }

    public List<Category> findAll() {
        return categoryJpaRepo.findAll();
    }

    public boolean existsByName(String name) {
        return categoryJpaRepo.existsByName(name);
    }

    public void delete(Category category) {
        categoryJpaRepo.delete(category);
    }
}
