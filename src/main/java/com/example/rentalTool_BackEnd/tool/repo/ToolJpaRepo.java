package com.example.rentalTool_BackEnd.tool.repo;

import com.example.rentalTool_BackEnd.tool.model.Tool;
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

//    Page<Tool> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
//            String name, String description, Pageable pageable);

    Page<Tool> findByIsActiveTrue(Pageable pageable);

    Page<Tool> findByIsActiveTrueAndNameContainingIgnoreCaseOrIsActiveTrueAndDescriptionContainingIgnoreCase(
            String name, String description, Pageable pageable);

    /**
     * Znajduje narzędzia według statusu moderacji
     */
    @Query("SELECT t FROM Tool t WHERE t.moderationStatus = :status ORDER BY t.createdAt DESC")
    Page<Tool> findByModerationStatus(@Param("status") ModerationStatus status, Pageable pageable);

    /**
     * Znajduje wszystkie zatwierdzone i aktywne narzędzia
     */
    @Query("SELECT t FROM Tool t WHERE t.moderationStatus = 'APPROVED' AND t.isActive = true ORDER BY t.updatedAt DESC")
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
}
