package com.example.rentalTool_BackEnd.tool.repo;

import com.example.rentalTool_BackEnd.tool.model.Tool;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
