package com.example.rentalTool_BackEnd.tool.web.controller;

import com.example.rentalTool_BackEnd.shared.model.HttpResponse;
import com.example.rentalTool_BackEnd.shared.util.TimeUtil;
import com.example.rentalTool_BackEnd.tool.model.ToolImage;
import com.example.rentalTool_BackEnd.tool.service.ToolService;
import com.example.rentalTool_BackEnd.tool.web.mapper.ToolImageMapper;
import com.example.rentalTool_BackEnd.tool.web.model.ToolImageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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


    @Operation(summary = "Prześlij zdjęcie narzędzia", description = "Dodaje nowe zdjęcie do narzędzia z opcją ustawienia jako główne")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Zdjęcie przesłane pomyślnie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "CREATED",
                                      "statusCode": 201,
                                      "reason": "Image upload request",
                                      "message": "Image uploaded successfully",
                                      "data": {
                                        "image": {
                                          "id": 1,
                                          "imageUrl": "/api/v1/files/image123.jpg",
                                          "isMain": true
                                        }
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Błąd walidacji - nieprawidłowy format pliku lub plik za duży",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "BAD_REQUEST",
                                      "statusCode": 400,
                                      "reason": "Invalid file",
                                      "message": "Invalid file format. Only JPG, PNG and WEBP are allowed"
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp - wymagane zalogowanie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "UNAUTHORIZED",
                                      "statusCode": 401,
                                      "reason": "Authorization failed",
                                      "message": "Unauthorized access - authentication required"
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień - nie jesteś właścicielem narzędzia",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "FORBIDDEN",
                                      "statusCode": 403,
                                      "reason": "Unauthorized access to tool",
                                      "message": "You are not the owner of this tool"
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Narzędzie nie znalezione",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "NOT_FOUND",
                                      "statusCode": 404,
                                      "reason": "Tool has not been found",
                                      "message": "Tool not found"
                                    }
                                    """))),
            @ApiResponse(responseCode = "413", description = "Plik za duży - maksymalny rozmiar to 10MB",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "PAYLOAD_TOO_LARGE",
                                      "statusCode": 413,
                                      "reason": "File too large",
                                      "message": "Maximum upload size exceeded"
                                    }
                                    """)))
    })
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

    @Operation(summary = "Pobierz zdjęcia narzędzia", description = "Zwraca listę wszystkich zdjęć przypisanych do narzędzia")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista zdjęć pobrana pomyślnie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "OK",
                                      "statusCode": 200,
                                      "reason": "Tool images data request",
                                      "message": "Tool images retrieved",
                                      "data": {
                                        "images": [
                                          {
                                            "id": 1,
                                            "imageUrl": "/api/v1/files/image123.jpg",
                                            "isMain": true
                                          },
                                          {
                                            "id": 2,
                                            "imageUrl": "/api/v1/files/image124.jpg",
                                            "isMain": false
                                          }
                                        ]
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Narzędzie nie znalezione",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "NOT_FOUND",
                                      "statusCode": 404,
                                      "reason": "Tool has not been found",
                                      "message": "Tool not found"
                                    }
                                    """)))
    })
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

    @Operation(summary = "Ustaw zdjęcie główne", description = "Ustawia wybrane zdjęcie jako główne dla narzędzia")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zdjęcie główne ustawione pomyślnie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "OK",
                                      "statusCode": 200,
                                      "reason": "Set main image request",
                                      "message": "Main image set successfully",
                                      "data": {
                                        "image": {
                                          "id": 2,
                                          "imageUrl": "/api/v1/files/image124.jpg",
                                          "isMain": true
                                        }
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp - wymagane zalogowanie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "UNAUTHORIZED",
                                      "statusCode": 401,
                                      "reason": "Authorization failed",
                                      "message": "Unauthorized access - authentication required"
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień - nie jesteś właścicielem narzędzia",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "FORBIDDEN",
                                      "statusCode": 403,
                                      "reason": "Unauthorized access to tool",
                                      "message": "You are not the owner of this tool"
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Narzędzie lub zdjęcie nie znalezione",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "NOT_FOUND",
                                      "statusCode": 404,
                                      "reason": "Image not found",
                                      "message": "Image not found or does not belong to this tool"
                                    }
                                    """)))
    })
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

    @Operation(summary = "Usuń zdjęcie narzędzia", description = "Usuwa zdjęcie z narzędzia")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zdjęcie usunięte pomyślnie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "OK",
                                      "statusCode": 200,
                                      "reason": "Image deletion request",
                                      "message": "Image deleted successfully",
                                      "data": {}
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp - wymagane zalogowanie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "UNAUTHORIZED",
                                      "statusCode": 401,
                                      "reason": "Authorization failed",
                                      "message": "Unauthorized access - authentication required"
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień - nie jesteś właścicielem narzędzia",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "FORBIDDEN",
                                      "statusCode": 403,
                                      "reason": "Unauthorized access to tool",
                                      "message": "You are not the owner of this tool"
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Narzędzie lub zdjęcie nie znalezione",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "NOT_FOUND",
                                      "statusCode": 404,
                                      "reason": "Image not found",
                                      "message": "Image not found or does not belong to this tool"
                                    }
                                    """)))
    })
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

