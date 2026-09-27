package offeria.material_service.service.impl;

import offeria.material_service.domain.entity.Material;
import offeria.material_service.domain.enums.MaterialSource;
import offeria.material_service.domain.enums.MaterialStatus;
import offeria.material_service.dto.request.MaterialRequestDTO;
import offeria.material_service.dto.response.MaterialResponseDTO;
import offeria.material_service.exception.ResourceNotFoundException;
import offeria.material_service.mapper.MaterialMapper;
import offeria.material_service.repository.MaterialRepository;
import offeria.material_service.service.normalization.MaterialNameNormalizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import offeria.material_service.domain.enums.AliasLanguage;
import offeria.material_service.repository.MaterialAliasRepository;
import offeria.material_service.domain.entity.MaterialAlias;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaterialServiceImplTest {

    @Mock
    private MaterialAliasRepository materialAliasRepository;

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private MaterialMapper materialMapper;

    @Mock
    private MaterialNameNormalizer materialNameNormalizer;

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
                .canonicalEnglishName("Steel")
                .preferredIraqiName("حديد")
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
    void createMaterial_ShouldSetDefaultStatusAndSource() {
        when(materialMapper.toEntity(any())).thenReturn(material);
        when(materialRepository.save(any())).thenReturn(material);
        when(materialMapper.toResponseDTO(any())).thenReturn(responseDTO);

        MaterialResponseDTO result = materialService.createMaterial(requestDTO);

        assertNotNull(result);
        assertEquals(materialId, result.getId());

        ArgumentCaptor<Material> materialCaptor =
                ArgumentCaptor.forClass(Material.class);

        verify(materialRepository, times(1)).save(materialCaptor.capture());

        Material savedMaterial = materialCaptor.getValue();

        assertEquals(MaterialStatus.PENDING_REVIEW, savedMaterial.getStatus());
        assertEquals(MaterialSource.MANUAL, savedMaterial.getSource());
    }

    @Test
    void createMaterial_ShouldPopulateNormalizedNames() {
        when(materialMapper.toEntity(requestDTO)).thenReturn(material);
        when(materialNameNormalizer.normalizeEnglish("Steel")).thenReturn("steel");
        when(materialNameNormalizer.normalizeArabic("حديد")).thenReturn("حديد");
        when(materialRepository.save(any(Material.class))).thenReturn(material);
        when(materialMapper.toResponseDTO(material)).thenReturn(responseDTO);

        materialService.createMaterial(requestDTO);

        ArgumentCaptor<Material> materialCaptor =
                ArgumentCaptor.forClass(Material.class);

        verify(materialRepository).save(materialCaptor.capture());

        Material savedMaterial = materialCaptor.getValue();

        assertEquals("steel", savedMaterial.getNormalizedEnglishName());
        assertEquals("حديد", savedMaterial.getNormalizedIraqiName());
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

    @Test
    void updateMaterial_ShouldRecalculateNormalizedNames() {
        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));

        doAnswer(invocation -> {
            Material target = invocation.getArgument(1);
            target.setCanonicalEnglishName("  OIL   Filter  ");
            target.setPreferredIraqiName("  فلتر   دهن  ");
            return null;
        }).when(materialMapper).updateEntity(requestDTO, material);

        when(materialNameNormalizer.normalizeEnglish("  OIL   Filter  "))
                .thenReturn("oil filter");
        when(materialNameNormalizer.normalizeArabic("  فلتر   دهن  "))
                .thenReturn("فلتر دهن");

        when(materialRepository.save(any(Material.class))).thenReturn(material);
        when(materialMapper.toResponseDTO(material)).thenReturn(responseDTO);

        materialService.updateMaterial(materialId, requestDTO);

        ArgumentCaptor<Material> materialCaptor =
                ArgumentCaptor.forClass(Material.class);

        verify(materialRepository).save(materialCaptor.capture());

        Material savedMaterial = materialCaptor.getValue();

        assertEquals("oil filter", savedMaterial.getNormalizedEnglishName());
        assertEquals("فلتر دهن", savedMaterial.getNormalizedIraqiName());
    }
    @Test
    void findByNormalizedName_WhenEnglishMaterialMatches_ShouldReturnMaterial() {
        when(materialNameNormalizer.normalize("  OIL   FILTER  ", AliasLanguage.ENGLISH))
                .thenReturn("oil filter");

        when(materialRepository.findByNormalizedEnglishName("oil filter"))
                .thenReturn(Optional.of(material));

        when(materialMapper.toResponseDTO(material))
                .thenReturn(responseDTO);

        Optional<MaterialResponseDTO> result =
                materialService.findByNormalizedName(
                        "  OIL   FILTER  ",
                        AliasLanguage.ENGLISH
                );

        assertTrue(result.isPresent());
        assertEquals(materialId, result.get().getId());

        verify(materialRepository)
                .findByNormalizedEnglishName("oil filter");

        verifyNoInteractions(materialAliasRepository);
    }
    @Test
    void findByNormalizedName_WhenDirectMaterialNotFound_ShouldReturnMaterialFromApprovedAlias() {
        MaterialAlias alias = MaterialAlias.builder()
                .material(material)
                .alias("Engine Oil Filter")
                .normalizedAlias("engine oil filter")
                .status(MaterialStatus.APPROVED)
                .source(MaterialSource.MANUAL)
                .build();

        when(materialNameNormalizer.normalize(
                "  ENGINE   OIL FILTER  ",
                AliasLanguage.ENGLISH
        )).thenReturn("engine oil filter");

        when(materialRepository.findByNormalizedEnglishName("engine oil filter"))
                .thenReturn(Optional.empty());

        when(materialAliasRepository.findByNormalizedAliasAndStatus(
                "engine oil filter",
                MaterialStatus.APPROVED
        )).thenReturn(Optional.of(alias));

        when(materialMapper.toResponseDTO(material))
                .thenReturn(responseDTO);

        Optional<MaterialResponseDTO> result =
                materialService.findByNormalizedName(
                        "  ENGINE   OIL FILTER  ",
                        AliasLanguage.ENGLISH
                );

        assertTrue(result.isPresent());
        assertEquals(materialId, result.get().getId());

        verify(materialRepository)
                .findByNormalizedEnglishName("engine oil filter");

        verify(materialAliasRepository)
                .findByNormalizedAliasAndStatus(
                        "engine oil filter",
                        MaterialStatus.APPROVED
                );
    }

    @Test
    void findByNormalizedName_WhenNoApprovedAliasExists_ShouldReturnEmpty() {
        when(materialNameNormalizer.normalize(
                "Pending Alias",
                AliasLanguage.ENGLISH
        )).thenReturn("pending alias");

        when(materialRepository.findByNormalizedEnglishName("pending alias"))
                .thenReturn(Optional.empty());

        when(materialAliasRepository.findByNormalizedAliasAndStatus(
                "pending alias",
                MaterialStatus.APPROVED
        )).thenReturn(Optional.empty());

        Optional<MaterialResponseDTO> result =
                materialService.findByNormalizedName(
                        "Pending Alias",
                        AliasLanguage.ENGLISH
                );

        assertTrue(result.isEmpty());

        verify(materialAliasRepository)
                .findByNormalizedAliasAndStatus(
                        "pending alias",
                        MaterialStatus.APPROVED
                );

        verify(materialAliasRepository, never())
                .findByNormalizedAliasAndStatus(
                        "pending alias",
                        MaterialStatus.PENDING_REVIEW
                );

        verify(materialAliasRepository, never())
                .findByNormalizedAliasAndStatus(
                        "pending alias",
                        MaterialStatus.REJECTED
                );

        verifyNoInteractions(materialMapper);
    }

    @Test
    void findByNormalizedName_WhenInputIsBlank_ShouldReturnEmptyWithoutRepositoryLookup() {
        when(materialNameNormalizer.normalize("   ", AliasLanguage.ENGLISH))
                .thenReturn(null);

        Optional<MaterialResponseDTO> result =
                materialService.findByNormalizedName(
                        "   ",
                        AliasLanguage.ENGLISH
                );

        assertTrue(result.isEmpty());

        verify(materialNameNormalizer)
                .normalize("   ", AliasLanguage.ENGLISH);

        verifyNoInteractions(materialRepository);
        verifyNoInteractions(materialAliasRepository);
        verifyNoInteractions(materialMapper);
    }

    @Test
    void findByNormalizedName_WhenInputIsNull_ShouldReturnEmptyWithoutRepositoryLookup() {
        when(materialNameNormalizer.normalize(null, AliasLanguage.ENGLISH))
                .thenReturn(null);

        Optional<MaterialResponseDTO> result =
                materialService.findByNormalizedName(
                        null,
                        AliasLanguage.ENGLISH
                );

        assertTrue(result.isEmpty());

        verify(materialNameNormalizer)
                .normalize(null, AliasLanguage.ENGLISH);

        verifyNoInteractions(materialRepository);
        verifyNoInteractions(materialAliasRepository);
        verifyNoInteractions(materialMapper);
    }

    @Test
    void findByNormalizedName_WhenArabicMaterialMatches_ShouldReturnMaterial() {
        when(materialNameNormalizer.normalize(
                "  فلتر   دهن  ",
                AliasLanguage.ARABIC
        )).thenReturn("فلتر دهن");

        when(materialRepository.findByNormalizedIraqiName("فلتر دهن"))
                .thenReturn(Optional.of(material));

        when(materialMapper.toResponseDTO(material))
                .thenReturn(responseDTO);

        Optional<MaterialResponseDTO> result =
                materialService.findByNormalizedName(
                        "  فلتر   دهن  ",
                        AliasLanguage.ARABIC
                );

        assertTrue(result.isPresent());
        assertEquals(materialId, result.get().getId());

        verify(materialRepository)
                .findByNormalizedIraqiName("فلتر دهن");

        verify(materialRepository, never())
                .findByNormalizedEnglishName(anyString());

        verifyNoInteractions(materialAliasRepository);
    }
}
