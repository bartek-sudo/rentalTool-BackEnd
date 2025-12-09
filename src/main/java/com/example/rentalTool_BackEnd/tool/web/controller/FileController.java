package com.example.rentalTool_BackEnd.tool.web.controller;

import com.example.rentalTool_BackEnd.tool.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @Operation(summary = "Pobierz plik", description = "Zwraca plik graficzny (zdjęcie narzędzia)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plik zwrócony pomyślnie",
                    content = @Content(mediaType = "image/*",
                            schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "404", description = "Plik nie znaleziony")
    })
    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> getFile(
            @PathVariable String fileName,
            HttpServletRequest request) {

        try {
            Resource resource = fileStorageService.loadFileAsResource(fileName);

            // Określenie typu zawartości
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                // logger.info("Could not determine file type.");
            }

            // Fallback do domyślnego typu
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
