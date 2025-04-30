package com.example.rentalTool_BackEnd.tool.spi;

import java.util.List;

public interface ToolExternalService {

    ToolExternalDto getToolDtoById(long id);

    List<ToolExternalDto> getToolsByOwnerId(long ownerId);
}
