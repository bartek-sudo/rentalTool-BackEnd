package com.example.rentalTool_BackEnd.tool.web.controller;

import com.example.rentalTool_BackEnd.shared.model.HttpResponse;
import com.example.rentalTool_BackEnd.shared.util.TimeUtil;
import com.example.rentalTool_BackEnd.tool.terms.exception.TermsNotFoundException;
import com.example.rentalTool_BackEnd.tool.terms.service.TermsService;
import com.example.rentalTool_BackEnd.tool.model.Tool;
import com.example.rentalTool_BackEnd.tool.service.ToolService;
import com.example.rentalTool_BackEnd.tool.web.mapper.ToolDtoMapper;
import com.example.rentalTool_BackEnd.tool.web.model.ToolDto;
import com.example.rentalTool_BackEnd.tool.web.requests.ToolCreateRequest;
import com.example.rentalTool_BackEnd.tool.web.requests.ToolUpdateRequest;
import com.example.rentalTool_BackEnd.tool.web.requests.ToolTermsUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/v1/tools")
@RequiredArgsConstructor
public class ToolController {
    private final ToolService toolService;
    private final ToolDtoMapper toolDtoMapper;
    private final TermsService termsService;

    @Operation(summary = "Pobierz narzędzie po ID", description = "Zwraca szczegóły narzędzia. Wymaga autoryzacji jeśli narzędzie nie jest publicznie widoczne")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Narzędzie pobrane pomyślnie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "OK",
                                      "statusCode": 200,
                                      "reason": "Tool data by id request",
                                      "message": "Tool by id",
                                      "data": {
                                        "Tool": {
                                          "id": 1,
                                          "name": "Wiertarka",
                                          "description": "Profesjonalna wiertarka",
                                          "pricePerDay": 50.0,
                                          "category": "POWER_TOOLS"
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
            @ApiResponse(responseCode = "404", description = "Narzędzie nie znalezione lub nie jest publicznie dostępne",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "NOT_FOUND",
                                      "statusCode": 404,
                                      "reason": "Tool not found or not available",
                                      "message": "Tool is not publicly available"
                                    }
                                    """)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<HttpResponse> getToolById(
            @PathVariable("id") long id,
            Authentication authentication) {
        final Tool tool = toolService.getToolById(id);

        boolean isOwner = false;
        boolean isModerator = false;
        if (authentication != null) {
            final Jwt jwt = (Jwt) authentication.getPrincipal();
            final long userId = jwt.getClaim("user_id");

            isOwner = tool.getOwnerId() == userId;
            isModerator = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("MODERATOR") || a.getAuthority().equals("ADMIN"));
        }

        if (!tool.isPubliclyVisible() && !(isOwner || isModerator)) {
            return ResponseEntity.status(404)
                    .body(HttpResponse.builder()
                            .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                            .statusCode(404)
                            .httpStatus(org.springframework.http.HttpStatus.NOT_FOUND)
                            .reason("Tool not found or not available")
                            .message("Tool is not publicly available")
                            .build());
        }

        return ResponseEntity.status(OK)
                .body(HttpResponse.builder()
                        .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                        .statusCode(OK.value())
                        .httpStatus(OK)
                        .reason("Tool data by id request")
                        .message("Tool by id")
                        .data(Map.of("Tool", toolDtoMapper.toDto(tool)))
                        .build());
    }

    @Operation(summary = "Wyszukaj narzędzia", description = "Wyszukuje narzędzia po nazwie/opisie z opcjonalnym filtrowaniem po kategorii i geolokalizacji. " +
            "Możliwe kategorie: GARDENING, CONSTRUCTION, ELECTRIC, PLUMBING, OTHER. " +
            "Jeśli podano latitude i longitude, wyniki zawierają odległość i są sortowane według odległości. " +
            "Parametr 'radius' określa promień w km (null = wszystkie narzędzia).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wyniki wyszukiwania",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "OK",
                                      "statusCode": 200,
                                      "reason": "Tools search request",
                                      "message": "Search results",
                                      "data": {
                                        "tools": [
                                          {
                                            "id": 1,
                                            "name": "Wiertarka",
                                            "description": "Profesjonalna wiertarka",
                                            "pricePerDay": 50.0,
                                            "category": "CONSTRUCTION",
                                            "distance": 12.45
                                          }
                                        ],
                                        "currentPage": 0,
                                        "totalPages": 1,
                                        "totalItems": 1,
                                        "pageSize": 10
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowa kategoria",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "BAD_REQUEST",
                                      "statusCode": 400,
                                      "reason": "Invalid argument",
                                      "message": "Invalid category: INVALID. Valid categories are: GARDENING, CONSTRUCTION, ELECTRIC, PLUMBING, OTHER"
                                    }
                                    """)))
    })
    @GetMapping("/search")
    public ResponseEntity<HttpResponse> searchTools(
            @Parameter(description = "Termin wyszukiwania (nazwa lub opis)", example = "wiertarka")
            @RequestParam(value = "search", required = false) String searchTerm,
            @Parameter(description = "Kategoria narzędzia (GARDENING, CONSTRUCTION, ELECTRIC, PLUMBING, OTHER)", example = "CONSTRUCTION")
            @RequestParam(value = "category", required = false) String category,
            @Parameter(description = "Szerokość geograficzna użytkownika (opcjonalne)", example = "50.0647")
            @RequestParam(value = "latitude", required = false) Double latitude,
            @Parameter(description = "Długość geograficzna użytkownika (opcjonalne)", example = "19.9450")
            @RequestParam(value = "longitude", required = false) Double longitude,
            @Parameter(description = "Promień wyszukiwania w km (opcjonalny, działa tylko z latitude/longitude)", example = "50")
            @RequestParam(value = "radius", required = false) Double radiusKm,
            @Parameter(description = "Numer strony (domyślnie 0)", example = "0")
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Rozmiar strony (domyślnie 10)", example = "10")
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Pole sortowania (domyślnie id, distance dla geolokalizacji, możliwe: id, name, pricePerDay, createdAt, updatedAt, distance)", example = "id")
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @Parameter(description = "Kierunek sortowania (asc/desc, domyślnie desc)", example = "desc")
            @RequestParam(value = "sortDirection", defaultValue = "desc") String sortDirection
    ) {
        // Sprawdź czy używamy geolokalizacji
        boolean useGeolocation = latitude != null && longitude != null;

        if (useGeolocation) {
            // Wyszukiwanie z geolokalizacją - uwzględniamy sortowanie
            Pageable pageable = PageRequest.of(
                    page,
                    size,
                    sortDirection.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending()
            );

            // Zabezpieczenie: jeśli radiusKm jest 0 lub ujemne, traktuj jako null (wszystkie)
            Double effectiveRadius = (radiusKm != null && radiusKm > 0) ? radiusKm : null;

            Page<Tool> toolsPage = toolService.findNearbyTools(latitude, longitude, effectiveRadius, searchTerm, category, pageable);

            // Konwertuj do DTO z odległością
            List<ToolDto> toolDtos = toolsPage.getContent().stream()
                    .map(tool -> {
                        Double distance = null;
                        if (tool.getLatitude() != null && tool.getLongitude() != null) {
                            distance = com.example.rentalTool_BackEnd.shared.util.GeoLocationUtil.roundDistance(
                                    com.example.rentalTool_BackEnd.shared.util.GeoLocationUtil.calculateDistance(
                                            latitude, longitude,
                                            tool.getLatitude(), tool.getLongitude()
                                    )
                            );
                        }
                        return toolDtoMapper.toDtoWithDistance(tool, distance);
                    })
                    .toList();

            return ResponseEntity.status(OK)
                    .body(HttpResponse.builder()
                            .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                            .statusCode(OK.value())
                            .httpStatus(OK)
                            .reason("Tools search request")
                            .message("Search results")
                            .data(Map.of("tools", toolDtos,
                                    "currentPage", toolsPage.getNumber(),
                                    "totalPages", toolsPage.getTotalPages(),
                                    "totalItems", toolsPage.getTotalElements(),
                                    "pageSize", toolsPage.getSize()
                            ))
                            .build());
        } else {
            // Zwykłe wyszukiwanie bez geolokalizacji
            Pageable pageable = PageRequest.of(
                    page,
                    size,
                    sortDirection.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending()
            );

            Page<Tool> toolsPage;

            boolean hasSearchTerm = searchTerm != null && !searchTerm.trim().isEmpty();
            boolean hasCategory = category != null && !category.trim().isEmpty();

            if (hasSearchTerm && hasCategory) {
                toolsPage = toolService.searchActiveTools(searchTerm, category, pageable);
            } else if (hasSearchTerm) {
                toolsPage = toolService.searchActiveTools(searchTerm, pageable);
            } else if (hasCategory) {
                toolsPage = toolService.getActiveToolsByCategory(category, pageable);
            } else {
                toolsPage = toolService.getActiveTools(pageable);
            }

            return ResponseEntity.status(OK)
                    .body(HttpResponse.builder()
                            .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                            .statusCode(OK.value())
                            .httpStatus(OK)
                            .reason("Tools search request")
                            .message("Search results")
                            .data(Map.of("tools", toolsPage.stream().map(toolDtoMapper::toDto).toList(),
                                    "currentPage", toolsPage.getNumber(),
                                    "totalPages", toolsPage.getTotalPages(),
                                    "totalItems", toolsPage.getTotalElements(),
                                    "pageSize", toolsPage.getSize()
                            ))
                            .build());
        }
    }

    @Operation(summary = "Utwórz narzędzie", description = "Tworzy nowe narzędzie (wymagane zalogowanie)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Narzędzie utworzone pomyślnie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "OK",
                                      "statusCode": 200,
                                      "reason": "Tool creation request",
                                      "message": "Tool created",
                                      "data": {
                                        "Tool": {
                                          "id": 1,
                                          "name": "Wiertarka",
                                          "description": "Profesjonalna wiertarka",
                                          "pricePerDay": 50.0,
                                          "category": "POWER_TOOLS"
                                        }
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Błąd walidacji danych",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "BAD_REQUEST",
                                      "statusCode": 400,
                                      "reason": "Validation failed",
                                      "message": "Validation failed",
                                      "data": {
                                        "validationErrors": {
                                          "name": "Name cannot be blank",
                                          "pricePerDay": "Price must be greater than 0"
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
                                    """)))
    })
    @PostMapping
    public ResponseEntity<HttpResponse> createTool(@RequestBody ToolCreateRequest toolCreateRequest, Authentication authentication) {
        final Jwt jwt = (Jwt) authentication.getPrincipal();

        final long userId = jwt.getClaim("user_id");

        Tool tool = toolService.createTool(toolCreateRequest, userId);
        return ResponseEntity.status(OK)
                .body(HttpResponse.builder()
                        .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                        .statusCode(OK.value())
                        .httpStatus(OK)
                        .reason("Tool creation request")
                        .message("Tool created")
                        .data(Map.of("Tool", toolDtoMapper.toDto(tool)))
                        .build());
    }

    @Operation(summary = "Aktualizuj narzędzie", description = "Aktualizuje istniejące narzędzie (tylko właściciel)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Narzędzie zaktualizowane pomyślnie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "OK",
                                      "statusCode": 200,
                                      "reason": "Tool update request",
                                      "message": "Tool updated successfully",
                                      "data": {
                                        "Tool": {
                                          "id": 1,
                                          "name": "Wiertarka udarowa",
                                          "description": "Zaktualizowany opis",
                                          "pricePerDay": 60.0
                                        }
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Błąd walidacji danych",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "BAD_REQUEST",
                                      "statusCode": 400,
                                      "reason": "Validation failed",
                                      "message": "Validation failed",
                                      "data": {
                                        "validationErrors": {
                                          "pricePerDay": "Price must be greater than 0",
                                          "description": "Description cannot be blank"
                                        }
                                      }
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
                                    """)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<HttpResponse> updateTool(
            @PathVariable("id") long toolId,
            @Valid @RequestBody ToolUpdateRequest toolUpdateRequest,
            Authentication authentication) {

        final Jwt jwt = (Jwt) authentication.getPrincipal();
        final long userId = jwt.getClaim("user_id");

        Tool updatedTool = toolService.updateTool(toolId, toolUpdateRequest, userId);

        return ResponseEntity.status(OK)
                .body(HttpResponse.builder()
                        .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                        .statusCode(OK.value())
                        .httpStatus(OK)
                        .reason("Tool update request")
                        .message("Tool updated successfully")
                        .data(Map.of("Tool", toolDtoMapper.toDto(updatedTool)))
                        .build());
    }

    @Operation(summary = "Zmień status narzędzia", description = "Aktywuje/deaktywuje narzędzie (tylko właściciel)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status narzędzia zmieniony pomyślnie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "OK",
                                      "statusCode": 200,
                                      "reason": "Tool status change request",
                                      "message": "Tool activated successfully",
                                      "data": {
                                        "Tool": {
                                          "id": 1,
                                          "name": "Wiertarka",
                                          "active": true
                                        }
                                      }
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
                                    """)))
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<HttpResponse> setToolStatus(
            @PathVariable("id") long toolId,
            @RequestParam("active") boolean active,
            Authentication authentication) {

        final Jwt jwt = (Jwt) authentication.getPrincipal();
        final long userId = jwt.getClaim("user_id");

        Tool updatedTool = toolService.setToolStatus(toolId, userId, active);

        String action = active ? "activated" : "deactivated";
        return ResponseEntity.status(OK)
                .body(HttpResponse.builder()
                        .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                        .statusCode(OK.value())
                        .httpStatus(OK)
                        .reason("Tool status change request")
                        .message("Tool " + action + " successfully")
                        .data(Map.of("Tool", toolDtoMapper.toDto(updatedTool)))
                        .build());
    }

    @Operation(summary = "Ustaw regulamin narzędzia", description = "Właściciel wybiera regulamin obowiązujący dla danego narzędzia")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Regulamin narzędzia zaktualizowany pomyślnie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "OK",
                                      "statusCode": 200,
                                      "reason": "Tool terms update request",
                                      "message": "Tool terms updated",
                                      "data": {
                                        "Tool": {
                                          "id": 1,
                                          "name": "Wiertarka",
                                          "termsId": 2
                                        }
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Regulamin nie znaleziony",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "BAD_REQUEST",
                                      "statusCode": 400,
                                      "reason": "Bad Request",
                                      "message": "Terms not found with id: 123"
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
                                    """)))
    })
    @PutMapping("/{id}/terms")
    public ResponseEntity<HttpResponse> updateToolTerms(
            @PathVariable("id") long toolId,
            @RequestBody ToolTermsUpdateRequest request,
            Authentication authentication) {

        final Jwt jwt = (Jwt) authentication.getPrincipal();
        final long userId = jwt.getClaim("user_id");

        if (request.termsId() != null) {
            try {
                termsService.getTermsById(request.termsId());
            } catch (TermsNotFoundException e) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST)
                        .body(HttpResponse.builder()
                                .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                                .statusCode(org.springframework.http.HttpStatus.BAD_REQUEST.value())
                                .httpStatus(org.springframework.http.HttpStatus.BAD_REQUEST)
                                .reason("Bad Request")
                                .message("Terms not found with id: " + request.termsId())
                                .build());
            }
        }

        Tool tool = toolService.updateToolTerms(toolId, userId, request.termsId());

        return ResponseEntity.status(OK)
                .body(HttpResponse.builder()
                        .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                        .statusCode(OK.value())
                        .httpStatus(OK)
                        .reason("Tool terms update request")
                        .message("Tool terms updated")
                        .data(Map.of("Tool", toolDtoMapper.toDto(tool)))
                        .build());
    }

    @Operation(summary = "Pobierz moje narzędzia", description = "Zwraca stronicowaną listę narzędzi należących do zalogowanego użytkownika")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista narzędzi pobrana pomyślnie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "OK",
                                      "statusCode": 200,
                                      "reason": "My tools data request",
                                      "message": "My tools",
                                      "data": {
                                        "tools": [
                                          {
                                            "id": 1,
                                            "name": "Wiertarka",
                                            "description": "Profesjonalna wiertarka",
                                            "pricePerDay": 50.0,
                                            "category": "CONSTRUCTION"
                                          }
                                        ],
                                        "currentPage": 0,
                                        "totalPages": 1,
                                        "totalItems": 5,
                                        "pageSize": 10
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
                                    """)))
    })
    @GetMapping("/my-tools")
    public ResponseEntity<HttpResponse> getMyTools(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "asc") String sortDirection,
            Authentication authentication) {

        final Jwt jwt = (Jwt) authentication.getPrincipal();
        final long userId = jwt.getClaim("user_id");

        Pageable pageable = PageRequest.of(
                page,
                size,
                sortDirection.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending()
        );

        Page<Tool> toolsPage = toolService.getToolsByOwnerId(userId, pageable);

        return ResponseEntity.status(OK)
                .body(HttpResponse.builder()
                        .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                        .statusCode(OK.value())
                        .httpStatus(OK)
                        .reason("My tools data request")
                        .message("My tools")
                        .data(Map.of("tools", toolsPage.stream().map(toolDtoMapper::toDto).toList(),
                                "currentPage", toolsPage.getNumber(),
                                "totalPages", toolsPage.getTotalPages(),
                                "totalItems", toolsPage.getTotalElements(),
                                "pageSize", toolsPage.getSize()
                        ))
                        .build());
    }
}
