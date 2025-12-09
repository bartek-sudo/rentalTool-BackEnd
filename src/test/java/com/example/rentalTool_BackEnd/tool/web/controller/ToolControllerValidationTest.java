package com.example.rentalTool_BackEnd.tool.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ToolControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testUpdateTool_WithBlankName_ShouldReturn400() throws Exception {
        String invalidRequest = """
                {
                    "name": "",
                    "description": "Test description",
                    "pricePerDay": 10.0,
                    "category": "Test category",
                    "address": "Test address",
                    "latitude": 52.0,
                    "longitude": 21.0
                }
                """;

        mockMvc.perform(put("/api/v1/tools/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest)
                        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.data.validationErrors.name").exists());
    }

    @Test
    void testUpdateTool_WithNullName_ShouldReturn400() throws Exception {
        String invalidRequest = """
                {
                    "name": null,
                    "description": "Test description",
                    "pricePerDay": 10.0,
                    "category": "Test category",
                    "address": "Test address",
                    "latitude": 52.0,
                    "longitude": 21.0
                }
                """;

        mockMvc.perform(put("/api/v1/tools/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest)
                        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data.validationErrors.name").exists());
    }

    @Test
    void testUpdateTool_WithNullLatitude_ShouldReturn400() throws Exception {
        String invalidRequest = """
                {
                    "name": "Test Tool",
                    "description": "Test description",
                    "pricePerDay": 10.0,
                    "category": "Test category",
                    "address": "Test address",
                    "latitude": null,
                    "longitude": 21.0
                }
                """;

        mockMvc.perform(put("/api/v1/tools/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest)
                        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data.validationErrors.latitude").exists());
    }

    @Test
    void testUpdateTool_WithNullLongitude_ShouldReturn400() throws Exception {
        String invalidRequest = """
                {
                    "name": "Test Tool",
                    "description": "Test description",
                    "pricePerDay": 10.0,
                    "category": "Test category",
                    "address": "Test address",
                    "latitude": 52.0,
                    "longitude": null
                }
                """;

        mockMvc.perform(put("/api/v1/tools/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest)
                        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data.validationErrors.longitude").exists());
    }

    @Test
    void testUpdateTool_WithMultipleValidationErrors_ShouldReturn400() throws Exception {
        String invalidRequest = """
                {
                    "name": "",
                    "description": "Test description",
                    "pricePerDay": 10.0,
                    "category": "Test category",
                    "address": "Test address",
                    "latitude": null,
                    "longitude": null
                }
                """;

        mockMvc.perform(put("/api/v1/tools/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest)
                        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data.validationErrors.name").exists())
                .andExpect(jsonPath("$.data.validationErrors.latitude").exists())
                .andExpect(jsonPath("$.data.validationErrors.longitude").exists());
    }

    @Test
    void testUpdateTool_WithMissingLatitude_ShouldReturn400() throws Exception {
        String invalidRequest = """
                {
                    "name": "Test Tool",
                    "description": "Test description",
                    "pricePerDay": 10.0,
                    "category": "Test category",
                    "address": "Test address",
                    "longitude": 21.0
                }
                """;

        mockMvc.perform(put("/api/v1/tools/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest)
                        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data.validationErrors.latitude").exists());
    }

    @Test
    void testUpdateTool_WithMissingLongitude_ShouldReturn400() throws Exception {
        String invalidRequest = """
                {
                    "name": "Test Tool",
                    "description": "Test description",
                    "pricePerDay": 10.0,
                    "category": "Test category",
                    "address": "Test address",
                    "latitude": 52.0
                }
                """;

        mockMvc.perform(put("/api/v1/tools/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest)
                        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data.validationErrors.longitude").exists());
    }
}

