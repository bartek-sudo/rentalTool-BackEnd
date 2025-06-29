package com.example.rentalTool_BackEnd.tool.web.requests;

import jakarta.validation.constraints.Size;

public record ModerationRequest(
        @Size(max = 500, message = "Comment cannot exceed 500 characters")
        String comment
) {
}
