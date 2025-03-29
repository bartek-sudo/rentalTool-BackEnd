package com.example.rentalTool_BackEnd.tool.service.impl;

import com.example.rentalTool_BackEnd.tool.model.Tool;
import com.example.rentalTool_BackEnd.tool.model.enums.Category;
import com.example.rentalTool_BackEnd.tool.repo.ToolRepo;
import com.example.rentalTool_BackEnd.tool.service.ToolService;
import com.example.rentalTool_BackEnd.tool.web.requests.ToolCreateRequest;
import com.example.rentalTool_BackEnd.user.model.User;
import com.example.rentalTool_BackEnd.user.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ToolServiceImpl implements ToolService {
    private final ToolRepo toolRepo;
    private final UserRepo userRepo;

    @Override
    public Tool getToolById(long id) {
        return toolRepo.findToolById(id)
                .orElseThrow(() -> new RuntimeException("Tool not found"));
    }

    @Override
    public Page<Tool> getAllTools(Pageable pageable){
        return toolRepo.findAllTools(pageable);
    }

    @Override
    public Tool createTool(ToolCreateRequest toolCreateRequest, String ownerEmail) {
        Category category = Category.valueOf(toolCreateRequest.category());

        User owner = userRepo.findUserByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toolRepo.saveTool(new Tool(toolCreateRequest.name(), toolCreateRequest.description(), toolCreateRequest.pricePerDay(), category, owner, toolCreateRequest.address(), toolCreateRequest.latitude(), toolCreateRequest.longitude()));
    }
}
