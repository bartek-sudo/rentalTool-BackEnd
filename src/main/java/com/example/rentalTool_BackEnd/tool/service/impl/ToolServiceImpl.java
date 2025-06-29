package com.example.rentalTool_BackEnd.tool.service.impl;

import com.example.rentalTool_BackEnd.tool.exception.ImageNotFoundException;
import com.example.rentalTool_BackEnd.tool.exception.ToolNotFoundException;
import com.example.rentalTool_BackEnd.tool.exception.UnauthorizedToolAccessException;
import com.example.rentalTool_BackEnd.tool.model.ToolImage;
import com.example.rentalTool_BackEnd.tool.model.enums.ModerationStatus;
import com.example.rentalTool_BackEnd.tool.repo.ToolImageRepo;
import com.example.rentalTool_BackEnd.tool.service.mapper.ToolExternalMapper;
import com.example.rentalTool_BackEnd.tool.model.Tool;
import com.example.rentalTool_BackEnd.tool.model.enums.Category;
import com.example.rentalTool_BackEnd.tool.repo.ToolRepo;
import com.example.rentalTool_BackEnd.tool.service.ToolService;
import com.example.rentalTool_BackEnd.tool.spi.ToolExternalDto;
import com.example.rentalTool_BackEnd.tool.spi.ToolExternalService;
import com.example.rentalTool_BackEnd.tool.web.requests.ToolCreateRequest;
import com.example.rentalTool_BackEnd.tool.web.requests.ToolUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@Service
@RequiredArgsConstructor
class ToolServiceImpl implements ToolService, ToolExternalService {
    private final ToolRepo toolRepo;
    private final ToolExternalMapper toolExternalMapper;
    private final FileStorageServiceImpl fileStorageService;
    private final ToolImageRepo toolImageRepo;

    @Override
    public Tool getToolById(long id) {
        return toolRepo.findToolById(id)
                .orElseThrow(() -> new ToolNotFoundException("Tool not found"));
    }

    @Override
    public ToolExternalDto getToolDtoById(long id) {
        Tool tool = toolRepo.findToolById(id)
                .orElseThrow(() -> new ToolNotFoundException("Tool not found"));
        return toolExternalMapper.toDto(tool);
    }

    @Override
    public Page<Tool> getActiveTools(Pageable pageable) {
        // Zwraca tylko zatwierdzone i aktywne narzędzia
        return toolRepo.findAllApprovedAndActiveTools(pageable);
    }
    @Override
    public Tool createTool(ToolCreateRequest toolCreateRequest, long ownerId) {
        Category category = Category.valueOf(toolCreateRequest.category());
        return toolRepo.saveTool(new Tool(toolCreateRequest.name(), toolCreateRequest.description(),
                toolCreateRequest.pricePerDay(), category, ownerId, toolCreateRequest.address(),
                toolCreateRequest.latitude(), toolCreateRequest.longitude()));
    }

    @Override
    public Tool updateTool(long toolId, ToolUpdateRequest toolUpdateRequest, long ownerId) {
        Tool tool = getToolById(toolId);

        if (tool.getOwnerId() != ownerId) {
            throw new UnauthorizedToolAccessException("You are not authorized to update this tool");
        }

        tool.setName(toolUpdateRequest.name());
        tool.setDescription(toolUpdateRequest.description());
        tool.setPricePerDay(toolUpdateRequest.pricePerDay());
        tool.setCategory(Category.valueOf(toolUpdateRequest.category()));
        tool.setAddress(toolUpdateRequest.address());
        tool.setLatitude(toolUpdateRequest.latitude());
        tool.setLongitude(toolUpdateRequest.longitude());

        tool.requiresRemoderation("Tool updated by owner");

        return toolRepo.saveTool(tool);
    }

    @Override
    public Tool deactivateTool(long toolId, long ownerId) {
        Tool tool = getToolById(toolId);
        if (tool.getOwnerId() != ownerId) {
            throw new UnauthorizedToolAccessException("You can only deactivate your own tools");
        }
        tool.setActive(false);
        return toolRepo.saveTool(tool);
    }

    @Override
    public Tool activateTool(long toolId, long ownerId) {
        Tool tool = getToolById(toolId);
        if (tool.getOwnerId() != ownerId) {
            throw new UnauthorizedToolAccessException("You can only activate your own tools");
        }

        tool.setActive(true);
        return toolRepo.saveTool(tool);
    }

    @Override
    public List<ToolExternalDto> getToolsByOwnerId(long ownerId) {
        List<Tool> tools = toolRepo.findToolsByOwnerId(ownerId);
        return tools.stream()
                .map(toolExternalMapper::toDto)
                .toList();
    }

    @Override
    public Page<Tool> searchActiveTools(String searchTerm, Pageable pageable) {
        // Wyszukuje tylko zatwierdzone i aktywne narzędzia
        return toolRepo.findApprovedToolsByNameOrDescription(searchTerm, searchTerm, pageable);
    }

    @Override
    public Page<Tool> getToolsByOwnerId(long ownerId, Pageable pageable) {
        return toolRepo.findByOwnerId(ownerId, pageable);
    }

    // Metody do obsługi zdjęć

    @Override
    @Transactional
    public ToolImage addImageToTool(long toolId, MultipartFile file, boolean isMain) {
        Tool tool = getToolById(toolId);

        boolean isFirstImage = tool.getImages().isEmpty();

        // Zapisz plik
        String fileName = fileStorageService.storeFile(file);

        // Utwórz URL dostępowy
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/files/")
                .path(fileName)
                .toUriString();

        // Utwórz obiekt zdjęcia
        ToolImage image = new ToolImage();
        image.setFilename(fileName);
        image.setUrl(fileDownloadUri);
        image.setContentType(file.getContentType());
        image.setTool(tool);

        // Logika głównego zdjęcia
        boolean shouldBeMain = isFirstImage || isMain;

        if (shouldBeMain) {
            // Zresetuj wszystkie inne zdjęcia na false
            tool.getImages().forEach(img -> img.setMain(false));
            image.setMain(true);
            tool.setMainImageUrl(image.getUrl());
        } else {
            image.setMain(false);
        }

        // Dodaj zdjęcie do narzędzia
        tool.addImage(image);

        // Zapisz narzędzie
        Tool savedTool = toolRepo.saveTool(tool);

        // Znajdź i zwróć zapisane zdjęcie
        return savedTool.getImages().stream()
                .filter(img -> img.getFilename().equals(fileName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Nie udało się znaleźć zapisanego zdjęcia"));
    }

    @Override
    @Transactional
    public void removeImageFromTool(long toolId, long imageId) {
        Tool tool = getToolById(toolId);

        // Znajdź zdjęcie do usunięcia
        ToolImage imageToRemove = tool.getImages().stream()
                .filter(img -> img.getId() == imageId)
                .findFirst()
                .orElseThrow(() -> new ImageNotFoundException("Image not found with id: " + imageId));

        // Usuń zdjęcie z narzędzia (metoda removeImage w Tool obsługuje logikę głównego zdjęcia)
        tool.removeImage(imageToRemove);

        // Zapisz zmiany
        toolRepo.saveTool(tool);

        // Usuń plik fizycznie
        fileStorageService.deleteFile(imageToRemove.getFilename());
    }

    @Override
    @Transactional
    public ToolImage setMainImage(long toolId, long imageId) {
        Tool tool = getToolById(toolId);

        // Znajdź zdjęcie, które ma być główne
        ToolImage newMainImage = tool.getImages().stream()
                .filter(img -> img.getId() == imageId)
                .findFirst()
                .orElseThrow(() -> new ImageNotFoundException("Image not found with id: " + imageId));

        // Użyj metody z modelu Tool
        tool.setMainImage(newMainImage);

        // Zapisz zmiany
        toolRepo.saveTool(tool);

        return newMainImage;
    }

    @Override
    public List<ToolImage> getToolImages(long toolId) {
        Tool tool = getToolById(toolId);
        return tool.getImages();
    }

    @Override
    public ToolImage getImageById(long imageId) {
        return toolImageRepo.findById(imageId)
                .orElseThrow(() -> new ImageNotFoundException("Image not found with id: " + imageId));
    }

    // ===== IMPLEMENTACJA METOD MODERACJI =====

    @Override
    public Page<Tool> getToolsPendingModeration(Pageable pageable) {
        return toolRepo.findByModerationStatus(ModerationStatus.PENDING, pageable);
    }

    @Override
    public Page<Tool> getToolsByModerationStatus(ModerationStatus status, Pageable pageable) {
        return toolRepo.findByModerationStatus(status, pageable);
    }

    @Transactional
    @Override
    public Tool approveTool(long toolId, long moderatorId, String comment) {
        Tool tool = getToolById(toolId);

        tool.approve(moderatorId, comment);
        return toolRepo.saveTool(tool);
    }

    @Transactional
    @Override
    public Tool rejectTool(long toolId, long moderatorId, String comment) {
        Tool tool = getToolById(toolId);

        if (comment == null || comment.trim().isEmpty()) {
            throw new IllegalArgumentException("Rejection comment is required");
        }

        tool.reject(moderatorId, comment);
        return toolRepo.saveTool(tool);
    }

    @Transactional
    @Override
    public Tool requireRemoderation(long toolId, String reason) {
        Tool tool = getToolById(toolId);

        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Remoderation reason is required");
        }

        tool.requiresRemoderation(reason);
        return toolRepo.saveTool(tool);
    }


}