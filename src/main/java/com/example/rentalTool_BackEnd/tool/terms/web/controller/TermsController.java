package com.example.rentalTool_BackEnd.tool.terms.web.controller;

import com.example.rentalTool_BackEnd.tool.category.model.Category;
import com.example.rentalTool_BackEnd.shared.model.HttpResponse;
import com.example.rentalTool_BackEnd.tool.category.service.CategoryService;
import com.example.rentalTool_BackEnd.tool.terms.model.Terms;
import com.example.rentalTool_BackEnd.tool.terms.service.TermsService;
import com.example.rentalTool_BackEnd.tool.terms.web.mapper.TermsMapper;
import com.example.rentalTool_BackEnd.tool.terms.web.requests.TermsRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/terms")
@RequiredArgsConstructor
public class TermsController {
    private final TermsService termsService;
    private final TermsMapper termsMapper;
    private final CategoryService categoryService;

    @Operation(summary = "Pobierz regulaminy dla kategorii", description = "Zwraca listę dostępnych regulaminów dla danej kategorii narzędzia (włącznie z regulaminami ogólnymi)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista regulaminów pobrana pomyślnie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "OK",
                                      "statusCode": 200,
                                      "reason": "Terms retrieved",
                                      "message": "Terms for category",
                                      "data": {
                                        "terms": [
                                          {
                                            "id": 1,
                                            "category": "CONSTRUCTION",
                                            "title": "Regulamin wypożyczenia narzędzi budowlanych",
                                            "content": "..."
                                          }
                                        ]
                                      }
                                    }
                                    """)))
    })
    @GetMapping("/category/{categoryName}")
    public ResponseEntity<HttpResponse> getTermsForCategory(@PathVariable("categoryName") String categoryName) {
        Category category = categoryService.getCategoryByName(categoryName);
        List<Terms> terms = termsService.getTermsForCategory(category);

        return ResponseEntity.status(HttpStatus.OK)
                .body(HttpResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .httpStatus(HttpStatus.OK)
                        .reason("Terms retrieved")
                        .message("Terms for category: " + categoryName)
                        .data(Map.of("terms", terms.stream()
                                .map(termsMapper::toDto)
                                .toList()))
                        .build());
    }

    @Operation(summary = "Pobierz wszystkie unikalne kategorie", description = "Zwraca listę wszystkich unikalnych kategorii regulaminów")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista kategorii pobrana pomyślnie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "OK",
                                      "statusCode": 200,
                                      "reason": "Categories retrieved",
                                      "message": "All unique categories",
                                      "data": {
                                        "categories": ["BUDOWLANE", "OGRODOWE", "ELEKTRONARZEDZIA"]
                                      }
                                    }
                                    """)))
    })
    @GetMapping("/categories")
    public ResponseEntity<HttpResponse> getAllCategories() {
        List<Category> categories = termsService.getCategoriesWithoutTerms();

        return ResponseEntity.status(HttpStatus.OK)
                .body(HttpResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .httpStatus(HttpStatus.OK)
                        .reason("Categories retrieved")
                        .message("All unique categories")
                        .data(Map.of("categories", categories.stream()
                                .map(c -> Map.of("id", c.getId(), "name", c.getName(), "displayName", c.getDisplayName()))
                                .toList()))
                        .build());
    }

    @Operation(summary = "Pobierz wszystkie regulaminy", description = "Zwraca listę wszystkich dostępnych regulaminów")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista regulaminów pobrana pomyślnie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "OK",
                                      "statusCode": 200,
                                      "reason": "Terms retrieved",
                                      "message": "All terms",
                                      "data": {
                                        "terms": [
                                          {
                                            "id": 1,
                                            "category": "GENERAL",
                                            "title": "Ogólny regulamin wypożyczania",
                                            "content": "..."
                                          }
                                        ]
                                      }
                                    }
                                    """)))
    })
    @GetMapping
    public ResponseEntity<HttpResponse> getAllTerms() {
        List<Terms> terms = termsService.getAllTerms();

        return ResponseEntity.status(HttpStatus.OK)
                .body(HttpResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .httpStatus(HttpStatus.OK)
                        .reason("Terms retrieved")
                        .message("All terms")
                        .data(Map.of("terms", terms.stream()
                                .map(termsMapper::toDto)
                                .toList()))
                        .build());
    }

    @Operation(summary = "Pobierz regulamin po ID", description = "Zwraca szczegóły konkretnego regulaminu")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Regulamin pobrany pomyślnie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "OK",
                                      "statusCode": 200,
                                      "reason": "Terms retrieved",
                                      "message": "Terms details",
                                      "data": {
                                        "terms": {
                                          "id": 1,
                                          "category": "CONSTRUCTION",
                                          "title": "Regulamin wypożyczenia narzędzi budowlanych",
                                          "content": "Pełna treść regulaminu..."
                                        }
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Regulamin nie znaleziony",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "NOT_FOUND",
                                      "statusCode": 404,
                                      "reason": "Terms not found",
                                      "message": "Terms not found with id: 123"
                                    }
                                    """)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<HttpResponse> getTermsById(@PathVariable("id") Long id) {
        Terms terms = termsService.getTermsById(id);

        return ResponseEntity.status(HttpStatus.OK)
                .body(HttpResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .httpStatus(HttpStatus.OK)
                        .reason("Terms retrieved")
                        .message("Terms details")
                        .data(Map.of("terms", termsMapper.toDto(terms)))
                        .build());
    }

    // ADMIN ENDPOINTS

    @Operation(summary = "[ADMIN] Utwórz nowy regulamin", description = "Tworzy nowy regulamin. Tylko dla ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Regulamin utworzony pomyślnie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "CREATED",
                                      "statusCode": 201,
                                      "reason": "Terms created",
                                      "message": "Terms created successfully",
                                      "data": {
                                        "terms": {
                                          "id": 1,
                                          "category": "CONSTRUCTION",
                                          "title": "Regulamin wypożyczenia narzędzi budowlanych",
                                          "content": "..."
                                        }
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class)))
    })
    @PostMapping
    public ResponseEntity<HttpResponse> createTerm(@Valid @RequestBody TermsRequest request) {
        try {
            Terms terms = termsService.createTerm(request.categoryId(), request.title(), request.content());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(HttpResponse.builder()
                            .statusCode(HttpStatus.CREATED.value())
                            .httpStatus(HttpStatus.CREATED)
                            .reason("Terms created")
                            .message("Terms created successfully")
                            .data(Map.of("terms", termsMapper.toDto(terms)))
                            .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(HttpResponse.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .httpStatus(HttpStatus.BAD_REQUEST)
                            .reason("Invalid data")
                            .message(e.getMessage())
                            .build());
        }
    }

    @Operation(summary = "[ADMIN] Aktualizuj regulamin", description = "Aktualizuje istniejący regulamin. Tylko dla ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Regulamin zaktualizowany pomyślnie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "OK",
                                      "statusCode": 200,
                                      "reason": "Terms updated",
                                      "message": "Terms updated successfully",
                                      "data": {
                                        "terms": {
                                          "id": 1,
                                          "category": "CONSTRUCTION",
                                          "title": "Regulamin wypożyczenia narzędzi budowlanych",
                                          "content": "..."
                                        }
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Regulamin nie znaleziony",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<HttpResponse> updateTerm(
            @PathVariable("id") Long id,
            @Valid @RequestBody TermsRequest request) {
        try {
            Terms terms = termsService.updateTerm(id, request.categoryId(), request.title(), request.content());

            return ResponseEntity.status(HttpStatus.OK)
                    .body(HttpResponse.builder()
                            .statusCode(HttpStatus.OK.value())
                            .httpStatus(HttpStatus.OK)
                            .reason("Terms updated")
                            .message("Terms updated successfully")
                            .data(Map.of("terms", termsMapper.toDto(terms)))
                            .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(HttpResponse.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .httpStatus(HttpStatus.BAD_REQUEST)
                            .reason("Invalid data")
                            .message(e.getMessage())
                            .build());
        }
    }

    @Operation(summary = "[ADMIN] Usuń regulamin", description = "Usuwa regulamin. Tylko dla ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Regulamin usunięty pomyślnie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "OK",
                                      "statusCode": 200,
                                      "reason": "Terms deleted",
                                      "message": "Terms deleted successfully"
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Regulamin nie znaleziony",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpResponse> deleteTerm(@PathVariable("id") Long id) {
        termsService.deleteTerm(id);

        return ResponseEntity.status(HttpStatus.OK)
                .body(HttpResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .httpStatus(HttpStatus.OK)
                        .reason("Terms deleted")
                        .message("Terms deleted successfully")
                        .build());
    }
}
