package offeria.material_service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import offeria.material_service.domain.entity.Material;
import offeria.material_service.dto.request.MaterialRequestDTO;
import offeria.material_service.dto.response.MaterialResponseDTO;
import offeria.material_service.exception.ResourceNotFoundException;
import offeria.material_service.mapper.MaterialMapper;
import offeria.material_service.repository.MaterialRepository;
import offeria.material_service.service.MaterialService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of MaterialService.
 * Handles business logic and coordinates between Repository and Mapper.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MaterialServiceImpl implements MaterialService {

    private final MaterialRepository materialRepository;
    private final MaterialMapper materialMapper;

    @Override
    @Transactional
    public MaterialResponseDTO createMaterial(MaterialRequestDTO requestDTO) {
        log.info("Creating new material: {}", requestDTO.getNameEn());
        Material material = materialMapper.toEntity(requestDTO);
        Material savedMaterial = materialRepository.save(material);
        return materialMapper.toResponseDTO(savedMaterial);
    }

    @Override
    @Transactional(readOnly = true)
    public MaterialResponseDTO getMaterialById(UUID id) {
        log.debug("Retrieving material by id: {}", id);
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found with id: " + id));
        return materialMapper.toResponseDTO(material);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MaterialResponseDTO> getAllMaterials(String query, Pageable pageable) {
        log.debug("Retrieving materials with query: {}, pageable: {}", query, pageable);
        Page<Material> materialPage;
        if (query != null && !query.trim().isEmpty()) {
            materialPage = materialRepository.searchByName(query, pageable);
        } else {
            materialPage = materialRepository.findAll(pageable);
        }
        return materialPage.map(materialMapper::toResponseDTO);
    }

    @Override
    @Transactional
    public MaterialResponseDTO updateMaterial(UUID id, MaterialRequestDTO requestDTO) {
        log.info("Updating material with id: {}", id);
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found with id: " + id));
        
        materialMapper.updateEntity(requestDTO, material);
        Material updatedMaterial = materialRepository.save(material);
        return materialMapper.toResponseDTO(updatedMaterial);
    }

    @Override
    @Transactional
    public void deleteMaterial(UUID id) {
        log.info("Deleting material with id: {}", id);
        if (!materialRepository.existsById(id)) {
            throw new ResourceNotFoundException("Material not found with id: " + id);
        }
        materialRepository.deleteById(id);
    }
}
