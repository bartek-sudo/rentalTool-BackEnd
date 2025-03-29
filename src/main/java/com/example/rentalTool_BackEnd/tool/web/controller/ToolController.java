package com.example.rentalTool_BackEnd.tool.web.controller;

import com.example.rentalTool_BackEnd.shared.model.HttpResponse;
import com.example.rentalTool_BackEnd.shared.util.TimeUtil;
import com.example.rentalTool_BackEnd.tool.model.Tool;
import com.example.rentalTool_BackEnd.tool.service.ToolService;
import com.example.rentalTool_BackEnd.tool.web.mapper.ToolDtoMapper;
import com.example.rentalTool_BackEnd.tool.web.requests.ToolCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/v1/tools")
@RequiredArgsConstructor
public class ToolController {
    private final ToolService toolService;
    private final ToolDtoMapper toolDtoMapper;

    @GetMapping("/{id}")
    public ResponseEntity<HttpResponse> getToolById(@PathVariable("id") long id) {
        final Tool tool = toolService.getToolById(id);
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

    @GetMapping("/all")
    public ResponseEntity<HttpResponse> getAllTools(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "asc") String sortDirection
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                sortDirection.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending()
        );

        Page<Tool> toolsPage = toolService.getAllTools(pageable);

        return ResponseEntity.status(OK)
                .body(HttpResponse.builder()
                        .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                        .statusCode(OK.value())
                        .httpStatus(OK)
                        .reason("All tools data request")
                        .message("All tools")
                        .data(Map.of("tools", toolsPage.stream().map(toolDtoMapper::toDto).toList(),
                                "currentPage", toolsPage.getNumber(),
                                "totalPages", toolsPage.getTotalPages(),
                                "totalItems", toolsPage.getTotalElements(),
                                "pageSize", toolsPage.getSize()
                                ))
                        .build());
    }

    @PostMapping("/create")
    public ResponseEntity<HttpResponse> createTool(@RequestBody ToolCreateRequest toolCreateRequest, Authentication authentication) {
        Tool tool = toolService.createTool(toolCreateRequest, authentication.getName());
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
}
