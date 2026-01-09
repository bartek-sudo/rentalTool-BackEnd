package com.example.rentalTool_BackEnd.tool.web.controller;

import com.example.rentalTool_BackEnd.shared.model.HttpResponse;
import com.example.rentalTool_BackEnd.shared.util.TimeUtil;
import com.example.rentalTool_BackEnd.tool.model.Tool;
import com.example.rentalTool_BackEnd.tool.model.enums.ModerationStatus;
import com.example.rentalTool_BackEnd.tool.service.ToolService;
import com.example.rentalTool_BackEnd.tool.web.mapper.ToolDtoMapper;
import com.example.rentalTool_BackEnd.tool.web.requests.ModerationRequest;
import io.swagger.v3.oas.annotations.Operation;
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

import java.util.Map;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/v1/moderation")
@RequiredArgsConstructor
public class ModerationController {

    private final ToolService toolService;
    private final ToolDtoMapper toolDtoMapper;

    @Operation(summary = "Pobierz narzędzia według statusu moderacji", description = "Zwraca narzędzia o określonym statusie moderacji (tylko moderator)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Narzędzia pobrane pomyślnie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "OK",
                                      "statusCode": 200,
                                      "reason": "Tools by status request",
                                      "message": "Tools with status: APPROVED",
                                      "data": {
                                        "tools": [],
                                        "currentPage": 0,
                                        "totalPages": 0,
                                        "totalItems": 0,
                                        "pageSize": 10,
                                        "status": "APPROVED"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowy status moderacji",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "BAD_REQUEST",
                                      "statusCode": 400,
                                      "reason": "Invalid moderation status",
                                      "message": "Valid statuses: PENDING, APPROVED, REJECTED"
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
    @GetMapping("/status/{status}")
    public ResponseEntity<HttpResponse> getToolsByStatus(
            @PathVariable("status") String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "moderatedAt") String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "desc") String sortDirection
    ) {
        try {
            ModerationStatus moderationStatus = ModerationStatus.valueOf(status.toUpperCase());

            Pageable pageable = PageRequest.of(
                    page,
                    size,
                    sortDirection.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending()
            );

            Page<Tool> toolsPage = toolService.getToolsByModerationStatus(moderationStatus, pageable);

            return ResponseEntity.status(OK)
                    .body(HttpResponse.builder()
                            .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                            .statusCode(OK.value())
                            .httpStatus(OK)
                            .reason("Tools by status request")
                            .message("Tools with status: " + status)
                            .data(Map.of(
                                    "tools", toolsPage.stream().map(toolDtoMapper::toDto).toList(),
                                    "currentPage", toolsPage.getNumber(),
                                    "totalPages", toolsPage.getTotalPages(),
                                    "totalItems", toolsPage.getTotalElements(),
                                    "pageSize", toolsPage.getSize(),
                                    "status", status
                            ))
                            .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(HttpResponse.builder()
                            .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                            .statusCode(400)
                            .httpStatus(org.springframework.http.HttpStatus.BAD_REQUEST)
                            .reason("Invalid moderation status")
                            .message("Valid statuses: PENDING, APPROVED, REJECTED")
                            .build());
        }
    }

    @Operation(summary = "Zatwierdź narzędzie", description = "Zatwierdza narzędzie do publikacji (tylko moderator)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Narzędzie zatwierdzone pomyślnie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "OK",
                                      "statusCode": 200,
                                      "reason": "Tool approval request",
                                      "message": "Tool approved successfully",
                                      "data": {
                                        "Tool": {
                                          "id": 1,
                                          "name": "Wiertarka",
                                          "moderationStatus": "APPROVED"
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
                                          "comment": "Comment cannot be blank"
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
    @PostMapping("/{toolId}/approve")
    public ResponseEntity<HttpResponse> approveTool(
            @PathVariable("toolId") long toolId,
            @Valid @RequestBody ModerationRequest moderationRequest,
            Authentication authentication
    ) {
        final Jwt jwt = (Jwt) authentication.getPrincipal();
        final long moderatorId = jwt.getClaim("user_id");

        Tool approvedTool = toolService.approveTool(toolId, moderatorId, moderationRequest.comment());

        return ResponseEntity.status(OK)
                .body(HttpResponse.builder()
                        .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                        .statusCode(OK.value())
                        .httpStatus(OK)
                        .reason("Tool approval request")
                        .message("Tool approved successfully")
                        .data(Map.of("Tool", toolDtoMapper.toDto(approvedTool)))
                        .build());
    }

    @Operation(summary = "Odrzuć narzędzie", description = "Odrzuca narzędzie (tylko moderator, wymagany komentarz)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Narzędzie odrzucone pomyślnie",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "OK",
                                      "statusCode": 200,
                                      "reason": "Tool rejection request",
                                      "message": "Tool rejected successfully",
                                      "data": {
                                        "Tool": {
                                          "id": 1,
                                          "name": "Wiertarka",
                                          "moderationStatus": "REJECTED"
                                        }
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Błąd walidacji - brak komentarza odrzucenia",
                    content = @Content(schema = @Schema(implementation = HttpResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2025-11-08T15:30:00",
                                      "httpStatus": "BAD_REQUEST",
                                      "statusCode": 400,
                                      "reason": "Invalid argument",
                                      "message": "Rejection comment is required"
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
    @PostMapping("/{toolId}/reject")
    public ResponseEntity<HttpResponse> rejectTool(
            @PathVariable("toolId") long toolId,
            @Valid @RequestBody ModerationRequest moderationRequest,
            Authentication authentication
    ) {
        final Jwt jwt = (Jwt) authentication.getPrincipal();
        final long moderatorId = jwt.getClaim("user_id");

        Tool rejectedTool = toolService.rejectTool(toolId, moderatorId, moderationRequest.comment());

        return ResponseEntity.status(OK)
                .body(HttpResponse.builder()
                        .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                        .statusCode(OK.value())
                        .httpStatus(OK)
                        .reason("Tool rejection request")
                        .message("Tool rejected successfully")
                        .data(Map.of("Tool", toolDtoMapper.toDto(rejectedTool)))
                        .build());
    }

}