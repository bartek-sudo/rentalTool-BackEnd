package com.example.rentalTool_BackEnd.tool.service;

import com.example.rentalTool_BackEnd.tool.model.Tool;
import com.example.rentalTool_BackEnd.tool.web.requests.ToolCreateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ToolService {

    Tool getToolById(long id);

    Tool createTool(ToolCreateRequest toolCreateRequest, String ownerEmail);

    Page<Tool> getAllTools(Pageable pageable);
}
