package offeria.material_service.controller.mcp;

import offeria.material_service.dto.mcp.McpMaterialQueryRequest;
import offeria.material_service.dto.mcp.McpMaterialToolResponse;
import offeria.material_service.service.mcp.McpMaterialToolsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class McpMaterialToolsControllerTest {

    private McpMaterialToolsService toolsService;
    private McpMaterialToolsController controller;

    @BeforeEach
    void setUp() {
        toolsService = mock(McpMaterialToolsService.class);
        controller = new McpMaterialToolsController(toolsService);
    }

    @Test
    void shouldGetMaterial() {
        UUID id = UUID.randomUUID();

        McpMaterialToolResponse response =
                response(id, "Steel Pipe");

        when(toolsService.getMaterial(id))
                .thenReturn(Optional.of(response));

        ResponseEntity<McpMaterialToolResponse> result =
                controller.getMaterial(id);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void shouldReturnNotFoundWhenMaterialDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(toolsService.getMaterial(id))
                .thenReturn(Optional.empty());

        assertEquals(
                HttpStatus.NOT_FOUND,
                controller.getMaterial(id).getStatusCode()
        );
    }

    @Test
    void shouldSearchMaterial() {
        McpMaterialToolResponse response =
                response(UUID.randomUUID(), "Steel Pipe");

        when(toolsService.searchMaterial("steel pipe"))
                .thenReturn(response);

        ResponseEntity<McpMaterialToolResponse> result =
                controller.searchMaterial(
                        new McpMaterialQueryRequest("steel pipe")
                );

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void shouldRejectBlankSearch() {
        ResponseEntity<McpMaterialToolResponse> result =
                controller.searchMaterial(
                        new McpMaterialQueryRequest(" ")
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                result.getStatusCode()
        );

        verifyNoInteractions(toolsService);
    }

    private McpMaterialToolResponse response(
            UUID id,
            String name
    ) {
        return new McpMaterialToolResponse(
                "search_material",
                name,
                true,
                id,
                name,
                "بايب حديد",
                "أنبوب فولاذي",
                "PCS",
                "Piping",
                "Carbon steel",
                "EXACT_ENGLISH_NAME",
                null,
                true,
                List.of()
        );
    }
}
