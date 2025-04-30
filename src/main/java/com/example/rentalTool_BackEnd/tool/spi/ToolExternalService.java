package com.example.rentalTool_BackEnd.tool.spi;

import com.example.rentalTool_BackEnd.tool.model.Tool;
import com.example.rentalTool_BackEnd.tool.web.requests.ToolCreateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ToolExternalService {

    ToolExternalDto getToolDtoById(long id);

    List<ToolExternalDto> getToolsByOwnerId(long ownerId);
}
