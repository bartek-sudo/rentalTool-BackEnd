package com.example.rentalTool_BackEnd.tool.repo;

import com.example.rentalTool_BackEnd.tool.model.ToolImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ToolImageRepo extends JpaRepository<ToolImage, Long> {
    List<ToolImage> findByToolId(long toolId);

    long countByToolId(long toolId);
}
