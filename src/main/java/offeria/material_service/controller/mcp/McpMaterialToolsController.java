package offeria.material_service.controller.mcp;

import lombok.RequiredArgsConstructor;
import offeria.material_service.dto.mcp.McpMaterialQueryRequest;
import offeria.material_service.dto.mcp.McpMaterialToolResponse;
import offeria.material_service.service.mcp.McpMaterialToolsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/mcp/materials")
@RequiredArgsConstructor
public class McpMaterialToolsController {

    private final McpMaterialToolsService toolsService;

    @GetMapping("/{materialId}")
    public ResponseEntity<McpMaterialToolResponse> getMaterial(
            @PathVariable UUID materialId
    ) {
        return toolsService.getMaterial(materialId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/search")
    public ResponseEntity<McpMaterialToolResponse> searchMaterial(
            @RequestBody McpMaterialQueryRequest request
    ) {
        if (request == null
                || request.query() == null
                || request.query().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(
                toolsService.searchMaterial(request.query())
        );
    }
}
