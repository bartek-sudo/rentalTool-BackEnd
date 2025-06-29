package com.example.rentalTool_BackEnd.tool.service;

import com.example.rentalTool_BackEnd.tool.model.Tool;
import com.example.rentalTool_BackEnd.tool.model.ToolImage;
import com.example.rentalTool_BackEnd.tool.model.enums.ModerationStatus;
import com.example.rentalTool_BackEnd.tool.web.requests.ToolCreateRequest;
import com.example.rentalTool_BackEnd.tool.web.requests.ToolUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ToolService {

    Tool getToolById(long id);

    Tool createTool(ToolCreateRequest toolCreateRequest, long ownerId);

    Tool updateTool(long toolId, ToolUpdateRequest toolUpdateRequest, long ownerId);

    Tool deactivateTool(long toolId, long ownerId);

    Tool activateTool(long toolId, long ownerId);

    Page<Tool> getActiveTools(Pageable pageable);

    Page<Tool> searchActiveTools(String searchTerm, Pageable pageable);

    Page<Tool> getToolsByOwnerId(long ownerId, Pageable pageable);

    // Metody do obsługi zdjęć
    ToolImage addImageToTool(long toolId, MultipartFile file, boolean isMain);

    void removeImageFromTool(long toolId, long imageId);

    ToolImage setMainImage(long toolId, long imageId);

    List<ToolImage> getToolImages(long toolId);

    ToolImage getImageById(long imageId);

    Page<Tool> getToolsPendingModeration(Pageable pageable);

    Page<Tool> getToolsByModerationStatus(ModerationStatus status, Pageable pageable);

    @Transactional
    Tool approveTool(long toolId, long moderatorId, String comment);

    @Transactional
    Tool rejectTool(long toolId, long moderatorId, String comment);

    @Transactional
    Tool requireRemoderation(long toolId, String reason);
}
