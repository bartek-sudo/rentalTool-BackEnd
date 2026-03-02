package com.example.rentalTool_BackEnd.tool.service.impl;

import com.example.rentalTool_BackEnd.shared.util.GeoLocationUtil;
import com.example.rentalTool_BackEnd.tool.category.model.Category;
import com.example.rentalTool_BackEnd.tool.category.service.CategoryService;
import com.example.rentalTool_BackEnd.tool.exception.ImageNotFoundException;
import com.example.rentalTool_BackEnd.tool.exception.ToolNotFoundException;
import com.example.rentalTool_BackEnd.tool.exception.UnauthorizedToolAccessException;
import com.example.rentalTool_BackEnd.tool.model.ToolImage;
import com.example.rentalTool_BackEnd.tool.model.enums.ModerationStatus;
import com.example.rentalTool_BackEnd.tool.repo.ToolImageRepo;
import com.example.rentalTool_BackEnd.tool.service.mapper.ToolExternalMapper;
import com.example.rentalTool_BackEnd.tool.model.Tool;
import com.example.rentalTool_BackEnd.tool.repo.ToolRepo;
import com.example.rentalTool_BackEnd.tool.service.ToolService;
import com.example.rentalTool_BackEnd.tool.spi.ToolExternalDto;
import com.example.rentalTool_BackEnd.tool.spi.ToolExternalService;
import com.example.rentalTool_BackEnd.tool.web.requests.ToolCreateRequest;
import com.example.rentalTool_BackEnd.tool.web.requests.ToolUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class ToolServiceImpl implements ToolService, ToolExternalService {
    private final ToolRepo toolRepo;
    private final ToolExternalMapper toolExternalMapper;
    private final FileStorageServiceImpl fileStorageService;
    private final ToolImageRepo toolImageRepo;
    private final CategoryService categoryService;

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
    public Page<Tool> getActiveToolsByCategory(String category, Pageable pageable) {
        Category categoryEntity = categoryService.getCategoryByName(category);
        return toolRepo.findAllApprovedAndActiveToolsByCategory(categoryEntity, pageable);
    }
    @Override
    public Tool createTool(ToolCreateRequest toolCreateRequest, long ownerId) {
        Category category = categoryService.getCategoryByName(toolCreateRequest.category());
        Tool tool = new Tool(toolCreateRequest.name(), toolCreateRequest.description(),
                toolCreateRequest.pricePerDay(), category, ownerId, toolCreateRequest.address(),
                toolCreateRequest.latitude(), toolCreateRequest.longitude(), toolCreateRequest.termsId());
        return toolRepo.saveTool(tool);
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
        tool.setCategory(categoryService.getCategoryByName(toolUpdateRequest.category()));
        tool.setAddress(toolUpdateRequest.address());
        tool.setLatitude(toolUpdateRequest.latitude());
        tool.setLongitude(toolUpdateRequest.longitude());
        tool.setTermsId(toolUpdateRequest.termsId());
        tool.setUpdatedAt(java.time.Instant.now());

        // Reset do PENDING - wymaga ponownej moderacji po edycji
        tool.setModerationStatus(ModerationStatus.PENDING);
        tool.setModeratorId(null);
        tool.setModeratedAt(null);
        tool.setModerationComment("Tool updated by owner - requires remoderation");
        tool.setActive(false);

        return toolRepo.saveTool(tool);
    }

    @Override
    public Tool setToolStatus(long toolId, long ownerId, boolean active) {
        Tool tool = getToolById(toolId);
        if (tool.getOwnerId() != ownerId) {
            throw new UnauthorizedToolAccessException("You can only change status of your own tools");
        }
        tool.setActive(active);
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
    public Page<Tool> searchActiveTools(String searchTerm, String category, Pageable pageable) {
        Category categoryEntity = categoryService.getCategoryByName(category);
        return toolRepo.findApprovedToolsByNameOrDescriptionAndCategory(searchTerm, searchTerm, categoryEntity, pageable);
    }

    @Override
    public Page<Tool> getToolsByOwnerId(long ownerId, Pageable pageable) {
        return toolRepo.findByOwnerId(ownerId, pageable);
    }

    @Override
    public Page<Tool> findNearbyTools(Double userLatitude, Double userLongitude, Double radiusKm,
                                      String searchTerm, String category, Pageable pageable) {
        Page<Tool> allToolsPage;

        boolean hasSearchTerm = searchTerm != null && !searchTerm.trim().isEmpty();
        boolean hasCategory = category != null && !category.trim().isEmpty();

        if (hasSearchTerm && hasCategory) {
            Category categoryEntity = categoryService.getCategoryByName(category);
            allToolsPage = toolRepo.findApprovedToolsByNameOrDescriptionAndCategory(searchTerm, searchTerm,
                    categoryEntity, Pageable.unpaged());
        } else if (hasSearchTerm) {
            allToolsPage = toolRepo.findApprovedToolsByNameOrDescription(searchTerm, searchTerm, Pageable.unpaged());
        } else if (hasCategory) {
            Category categoryEntity = categoryService.getCategoryByName(category);
            allToolsPage = toolRepo.findAllApprovedAndActiveToolsByCategory(categoryEntity, Pageable.unpaged());
        } else {
            allToolsPage = toolRepo.findAllApprovedAndActiveTools(Pageable.unpaged());
        }

        List<Tool> allTools = allToolsPage.getContent();

        // Filtruj narzędzia według odległości jeśli podano promień
        List<Tool> filteredTools;
        if (radiusKm != null && userLatitude != null && userLongitude != null) {
            filteredTools = allTools.stream()
                    .filter(tool -> {
                        if (tool.getLatitude() == null || tool.getLongitude() == null) {
                            return false; // Pomiń narzędzia bez lokalizacji
                        }
                        double distance = GeoLocationUtil.calculateDistance(
                                userLatitude, userLongitude,
                                tool.getLatitude(), tool.getLongitude()
                        );
                        return distance <= radiusKm;
                    })
                    .collect(Collectors.toList());
        } else {
            // Jeśli radius = null (nieskończoność), zwróć wszystkie
            filteredTools = allTools.stream().collect(Collectors.toList());
        }

        // Sortuj narzędzia według parametrów z Pageable lub domyślnie według odległości
        if (pageable.getSort().isSorted()) {
            // Jeśli użytkownik wybrał sortowanie, użyj go
            pageable.getSort().forEach(order -> {
                Comparator<Tool> comparator = getComparatorForField(order.getProperty(), userLatitude, userLongitude);
                if (order.isDescending()) {
                    comparator = comparator.reversed();
                }
                filteredTools.sort(comparator);
            });
        } else if (userLatitude != null && userLongitude != null) {
            // Domyślnie sortuj według odległości jeśli podano lokalizację użytkownika
            filteredTools.sort(Comparator.comparingDouble(tool -> {
                if (tool.getLatitude() == null || tool.getLongitude() == null) {
                    return Double.MAX_VALUE; // Narzędzia bez lokalizacji na końcu
                }
                return GeoLocationUtil.calculateDistance(
                        userLatitude, userLongitude,
                        tool.getLatitude(), tool.getLongitude()
                );
            }));
        }

        // Implementacja manualnej paginacji
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredTools.size());

        List<Tool> pageContent = filteredTools.subList(start, end);

        return new PageImpl<>(pageContent, pageable, filteredTools.size());
    }

    /**
     * Zwraca komparator dla danego pola sortowania
     */
    private Comparator<Tool> getComparatorForField(String field, Double userLatitude, Double userLongitude) {
        return switch (field.toLowerCase()) {
            case "name" -> Comparator.comparing(Tool::getName, String.CASE_INSENSITIVE_ORDER);
            case "priceperday" -> Comparator.comparing(Tool::getPricePerDay);
            case "createdat" -> Comparator.comparing(Tool::getCreatedAt);
            case "updatedat" -> Comparator.comparing(Tool::getUpdatedAt);
            case "id" -> Comparator.comparing(Tool::getId);
            case "distance" -> {
                if (userLatitude == null || userLongitude == null) {
                    // Jeśli nie ma lokalizacji, sortuj według ID jako fallback
                    yield Comparator.comparing(Tool::getId);
                }
                yield Comparator.comparingDouble(tool -> {
                    if (tool.getLatitude() == null || tool.getLongitude() == null) {
                        return Double.MAX_VALUE; // Narzędzia bez lokalizacji na końcu
                    }
                    return GeoLocationUtil.calculateDistance(
                            userLatitude, userLongitude,
                            tool.getLatitude(), tool.getLongitude()
                    );
                });
            }
            default -> Comparator.comparing(Tool::getId); // Domyślnie sortuj według ID
        };
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

    @Override
    public Tool updateToolTerms(long toolId, long ownerId, Long termsId) {
        Tool tool = getToolById(toolId);
        if (tool.getOwnerId() != ownerId) {
            throw new UnauthorizedToolAccessException("You are not authorized to update this tool");
        }
        tool.setTermsId(termsId);
        tool.setUpdatedAt(java.time.Instant.now());
        return toolRepo.saveTool(tool);
    }

    // ===== IMPLEMENTACJA METOD MODERACJI =====

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

}