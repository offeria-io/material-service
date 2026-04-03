package offeria.material_service.service;

import offeria.material_service.dto.request.MaterialRequestDTO;
import offeria.material_service.dto.response.MaterialResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for Material operations.
 */
public interface MaterialService {

    /**
     * Creates a new material.
     */
    MaterialResponseDTO createMaterial(MaterialRequestDTO requestDTO);

    /**
     * Retrieves a material by its unique identifier.
     */
    MaterialResponseDTO getMaterialById(UUID id);

    /**
     * Retrieves all materials with pagination and optional search query.
     */
    Page<MaterialResponseDTO> getAllMaterials(String query, Pageable pageable);

    /**
     * Updates an existing material.
     */
    MaterialResponseDTO updateMaterial(UUID id, MaterialRequestDTO requestDTO);

    /**
     * Deletes a material from the system.
     */
    void deleteMaterial(UUID id);
}
