package offeria.material_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import offeria.material_service.dto.request.MaterialRequestDTO;
import offeria.material_service.dto.response.MaterialResponseDTO;
import offeria.material_service.service.MaterialService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for managing materials.
 * Exposes endpoints for CRUD operations and search.
 */
@RestController
@RequestMapping("/api/v1/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    /**
     * Endpoint to create a new material.
     */
    @PostMapping
    public ResponseEntity<MaterialResponseDTO> createMaterial(@Valid @RequestBody MaterialRequestDTO requestDTO) {
        return new ResponseEntity<>(materialService.createMaterial(requestDTO), HttpStatus.CREATED);
    }

    /**
     * Endpoint to retrieve a material by its ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MaterialResponseDTO> getMaterialById(@PathVariable UUID id) {
        return ResponseEntity.ok(materialService.getMaterialById(id));
    }

    /**
     * Endpoint to list all materials with pagination and search functionality.
     */
    @GetMapping
    public ResponseEntity<Page<MaterialResponseDTO>> getAllMaterials(
            @RequestParam(required = false) String query,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(materialService.getAllMaterials(query, pageable));
    }

    /**
     * Endpoint to update an existing material.
     */
    @PutMapping("/{id}")
    public ResponseEntity<MaterialResponseDTO> updateMaterial(
            @PathVariable UUID id,
            @Valid @RequestBody MaterialRequestDTO requestDTO) {
        return ResponseEntity.ok(materialService.updateMaterial(id, requestDTO));
    }

    /**
     * Endpoint to delete a material by its ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaterial(@PathVariable UUID id) {
        materialService.deleteMaterial(id);
        return ResponseEntity.noContent().build();
    }
}
