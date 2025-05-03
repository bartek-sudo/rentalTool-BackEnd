package com.example.rentalTool_BackEnd.tool.web.controller;

import com.example.rentalTool_BackEnd.shared.model.HttpResponse;
import com.example.rentalTool_BackEnd.shared.util.TimeUtil;
import com.example.rentalTool_BackEnd.tool.model.ToolImage;
import com.example.rentalTool_BackEnd.tool.service.ToolService;
import com.example.rentalTool_BackEnd.tool.web.mapper.ToolImageMapper;
import com.example.rentalTool_BackEnd.tool.web.model.ToolImageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/v1/tools/{toolId}/images")
@RequiredArgsConstructor
public class ToolImageController {
    private final ToolService toolService;
    private final ToolImageMapper toolImageMapper;


    @PostMapping
    public ResponseEntity<HttpResponse> uploadImage(
            @PathVariable long toolId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "isMain", defaultValue = "false") boolean isMain) {

        ToolImage image = toolService.addImageToTool(toolId, file, isMain);

        return ResponseEntity.status(CREATED)
                .body(HttpResponse.builder()
                        .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                        .statusCode(CREATED.value())
                        .httpStatus(CREATED)
                        .reason("Image upload request")
                        .message("Image uploaded successfully")
                        .data(Map.of("image", toolImageMapper.toDto(image)))
                        .build());
    }

    @GetMapping
    public ResponseEntity<HttpResponse> getToolImages(@PathVariable long toolId) {
        List<ToolImage> images = toolService.getToolImages(toolId);
        List<ToolImageDto> imageDtos = images.stream()
                .map(toolImageMapper::toDto)
                .toList();

        return ResponseEntity.status(OK)
                .body(HttpResponse.builder()
                        .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                        .statusCode(OK.value())
                        .httpStatus(OK)
                        .reason("Tool images data request")
                        .message("Tool images retrieved")
                        .data(Map.of("images", imageDtos))
                        .build());
    }

    @PutMapping("/{imageId}/main")
    public ResponseEntity<HttpResponse> setMainImage(
            @PathVariable long toolId,
            @PathVariable long imageId) {

        ToolImage mainImage = toolService.setMainImage(toolId, imageId);

        return ResponseEntity.status(OK)
                .body(HttpResponse.builder()
                        .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                        .statusCode(OK.value())
                        .httpStatus(OK)
                        .reason("Set main image request")
                        .message("Main image set successfully")
                        .data(Map.of("image", toolImageMapper.toDto(mainImage)))
                        .build());
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<HttpResponse> deleteImage(
            @PathVariable long toolId,
            @PathVariable long imageId) {

        toolService.removeImageFromTool(toolId, imageId);

        return ResponseEntity.status(OK)
                .body(HttpResponse.builder()
                        .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                        .statusCode(OK.value())
                        .httpStatus(OK)
                        .reason("Image deletion request")
                        .message("Image deleted successfully")
                        .data(Map.of())
                        .build());
    }
}

