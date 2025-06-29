package com.example.rentalTool_BackEnd.tool.repo;

import com.example.rentalTool_BackEnd.tool.model.Tool;
import com.example.rentalTool_BackEnd.tool.model.enums.ModerationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ToolRepo {

    private final ToolJpaRepo toolJpaRepo;

    public Tool saveTool(Tool tool) {
        return toolJpaRepo.save(tool);
    }

    public Optional<Tool> findToolById(long id){
        return toolJpaRepo.findById(id);
    }

    public Page<Tool> findAllActiveTools(Pageable pageable){
        return toolJpaRepo.findByIsActiveTrue(pageable);
    }

    public List<Tool> findToolsByOwnerId(long ownerId) {
        return toolJpaRepo.findByOwnerId(ownerId);
    }

    public Page<Tool> findToolsByNameOrDescription(String name, String description, Pageable pageable) {
        return toolJpaRepo.findByIsActiveTrueAndNameContainingIgnoreCaseOrIsActiveTrueAndDescriptionContainingIgnoreCase(name, description, pageable);
    }

    public Page<Tool> findByOwnerId(long ownerId, Pageable pageable) {
        return toolJpaRepo.findByOwnerId(ownerId, pageable);
    }

    public Page<Tool> findByModerationStatus(ModerationStatus status, Pageable pageable) {
        return toolJpaRepo.findByModerationStatus(status, pageable);
    }

    public Page<Tool> findAllApprovedAndActiveTools(Pageable pageable) {
        return toolJpaRepo.findAllApprovedAndActiveTools(pageable);
    }

    public Page<Tool> findApprovedToolsByNameOrDescription(String name, String description, Pageable pageable) {
        return toolJpaRepo.findApprovedToolsByNameOrDescription(name, description, pageable);
    }


}
