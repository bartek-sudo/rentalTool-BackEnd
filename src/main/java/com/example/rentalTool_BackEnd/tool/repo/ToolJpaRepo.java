package com.example.rentalTool_BackEnd.tool.repo;

import com.example.rentalTool_BackEnd.tool.model.Tool;
import com.example.rentalTool_BackEnd.tool.category.model.Category;
import com.example.rentalTool_BackEnd.tool.model.enums.ModerationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
interface ToolJpaRepo extends JpaRepository<Tool, Long> {
    List<Tool> findByOwnerId(long ownerId);

    Page<Tool> findByOwnerId(long ownerId, Pageable pageable);

    /**
     * Znajduje narzędzia według statusu moderacji
     */
    @Query("SELECT t FROM Tool t WHERE t.moderationStatus = :status")
    Page<Tool> findByModerationStatus(@Param("status") ModerationStatus status, Pageable pageable);

    /**
     * Znajduje wszystkie zatwierdzone i aktywne narzędzia
     */
    @Query("SELECT t FROM Tool t WHERE t.moderationStatus = 'APPROVED' AND t.isActive = true")
    Page<Tool> findAllApprovedAndActiveTools(Pageable pageable);

    /**
     * Wyszukuje zatwierdzone narzędzia po nazwie lub opisie
     */
    @Query("SELECT t FROM Tool t WHERE t.moderationStatus = 'APPROVED' AND t.isActive = true AND " +
            "(LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(t.description) LIKE LOWER(CONCAT('%', :description, '%')))")
    Page<Tool> findApprovedToolsByNameOrDescription(
            @Param("name") String name,
            @Param("description") String description,
            Pageable pageable
    );

    /**
     * Wyszukuje zatwierdzone narzędzia po nazwie lub opisie z filtrowaniem po kategorii
     */
    @Query("SELECT t FROM Tool t WHERE t.moderationStatus = 'APPROVED' AND t.isActive = true AND " +
            "t.category = :category AND " +
            "(LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(t.description) LIKE LOWER(CONCAT('%', :description, '%')))")
    Page<Tool> findApprovedToolsByNameOrDescriptionAndCategory(
            @Param("name") String name,
            @Param("description") String description,
            @Param("category") Category category,
            Pageable pageable
    );

    /**
     * Znajduje zatwierdzone i aktywne narzędzia z filtrowaniem po kategorii
     */
    @Query("SELECT t FROM Tool t WHERE t.moderationStatus = 'APPROVED' AND t.isActive = true AND t.category = :category")
    Page<Tool> findAllApprovedAndActiveToolsByCategory(@Param("category") Category category, Pageable pageable);
}
