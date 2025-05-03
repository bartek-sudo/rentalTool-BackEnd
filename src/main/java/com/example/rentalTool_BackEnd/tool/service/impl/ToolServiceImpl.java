package com.example.rentalTool_BackEnd.tool.service.impl;

import com.example.rentalTool_BackEnd.tool.exception.ImageNotFoundException;
import com.example.rentalTool_BackEnd.tool.exception.ToolNotFoundException;
import com.example.rentalTool_BackEnd.tool.model.ToolImage;
import com.example.rentalTool_BackEnd.tool.repo.ToolImageRepo;
import com.example.rentalTool_BackEnd.tool.service.mapper.ToolExternalMapper;
import com.example.rentalTool_BackEnd.tool.model.Tool;
import com.example.rentalTool_BackEnd.tool.model.enums.Category;
import com.example.rentalTool_BackEnd.tool.repo.ToolRepo;
import com.example.rentalTool_BackEnd.tool.service.ToolService;
import com.example.rentalTool_BackEnd.tool.spi.ToolExternalDto;
import com.example.rentalTool_BackEnd.tool.spi.ToolExternalService;
import com.example.rentalTool_BackEnd.tool.web.requests.ToolCreateRequest;

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
    public Page<Tool> getAllTools(Pageable pageable){
        return toolRepo.findAllTools(pageable);
    }

    @Override
    public Tool createTool(ToolCreateRequest toolCreateRequest, long ownerId) {
        Category category = Category.valueOf(toolCreateRequest.category());

        return toolRepo.saveTool(new Tool(toolCreateRequest.name(), toolCreateRequest.description(), toolCreateRequest.pricePerDay(), category, ownerId, toolCreateRequest.address(), toolCreateRequest.latitude(), toolCreateRequest.longitude()));
    }

    @Override
    public List<ToolExternalDto> getToolsByOwnerId(long ownerId) {
        List<Tool> tools = toolRepo.findToolsByOwnerId(ownerId);
        return tools.stream()
                .map(toolExternalMapper::toDto)
                .toList();
    }

    // Poniżej nowe metody do obsługi zdjęć

    @Override
    @Transactional
    public ToolImage addImageToTool(long toolId, MultipartFile file, boolean isMain) {
        Tool tool = getToolById(toolId);

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
        image.setMain(isMain);
        image.setTool(tool);

        // Dodaj zdjęcie do narzędzia
        tool.addImage(image);

        // Zapisz zaktualizowane narzędzie
        toolRepo.saveTool(tool);

        return image;
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

        // Usuń zdjęcie z narzędzia
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

        // Ustaw jako główne
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

}
