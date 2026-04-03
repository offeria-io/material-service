package offeria.material_service.service.impl;

import offeria.material_service.domain.entity.Material;
import offeria.material_service.dto.request.MaterialRequestDTO;
import offeria.material_service.dto.response.MaterialResponseDTO;
import offeria.material_service.exception.ResourceNotFoundException;
import offeria.material_service.mapper.MaterialMapper;
import offeria.material_service.repository.MaterialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaterialServiceImplTest {

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private MaterialMapper materialMapper;

    @InjectMocks
    private MaterialServiceImpl materialService;

    private Material material;
    private MaterialRequestDTO requestDTO;
    private MaterialResponseDTO responseDTO;
    private UUID materialId;

    @BeforeEach
    void setUp() {
        materialId = UUID.randomUUID();
        material = Material.builder()
                .id(materialId)
                .nameEn("Steel")
                .nameAr("حديد")
                .unit("kg")
                .build();

        requestDTO = MaterialRequestDTO.builder()
                .nameEn("Steel")
                .nameAr("حديد")
                .unit("kg")
                .build();

        responseDTO = MaterialResponseDTO.builder()
                .id(materialId)
                .nameEn("Steel")
                .nameAr("حديد")
                .unit("kg")
                .build();
    }

    @Test
    void createMaterial_ShouldReturnSavedMaterial() {
        when(materialMapper.toEntity(any())).thenReturn(material);
        when(materialRepository.save(any())).thenReturn(material);
        when(materialMapper.toResponseDTO(any())).thenReturn(responseDTO);

        MaterialResponseDTO result = materialService.createMaterial(requestDTO);

        assertNotNull(result);
        assertEquals(materialId, result.getId());
        verify(materialRepository, times(1)).save(any());
    }

    @Test
    void getMaterialById_WhenFound_ShouldReturnMaterial() {
        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
        when(materialMapper.toResponseDTO(material)).thenReturn(responseDTO);

        MaterialResponseDTO result = materialService.getMaterialById(materialId);

        assertNotNull(result);
        assertEquals("Steel", result.getNameEn());
    }

    @Test
    void getMaterialById_WhenNotFound_ShouldThrowException() {
        when(materialRepository.findById(materialId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> materialService.getMaterialById(materialId));
    }
}
