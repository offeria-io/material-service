package offeria.material_service.service.translation;

import offeria.material_service.domain.entity.Material;
import offeria.material_service.domain.enums.MaterialMatchType;
import offeria.material_service.domain.enums.MaterialSource;
import offeria.material_service.domain.enums.MaterialStatus;
import offeria.material_service.dto.response.MaterialMatchResponse;
import offeria.material_service.dto.response.MaterialTranslationKnowledgeResponse;
import offeria.material_service.repository.MaterialRepository;
import offeria.material_service.service.matching.MaterialMatchingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaterialTranslationKnowledgeServiceTest {

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private MaterialMatchingService matchingService;

    private MaterialTranslationKnowledgeService service;

    @BeforeEach
    void setUp() {
        service = new MaterialTranslationKnowledgeService(
                materialRepository,
                matchingService
        );
    }

    @Test
    void shouldFindTranslationKnowledgeByMaterialId() {
        UUID materialId = UUID.randomUUID();
        Material material = material(materialId);

        when(materialRepository.findById(materialId))
                .thenReturn(Optional.of(material));

        Optional<MaterialTranslationKnowledgeResponse> result =
                service.findByMaterialId(materialId);

        assertTrue(result.isPresent());
        assertEquals(materialId, result.get().materialId());
        assertEquals("Steel Pipe", result.get().canonicalEnglishName());
        assertEquals("بايب حديد", result.get().preferredIraqiName());
        assertEquals("أنبوب فولاذي", result.get().standardArabicName());
    }

    @Test
    void shouldReturnEmptyWhenMaterialIdDoesNotExist() {
        UUID materialId = UUID.randomUUID();

        when(materialRepository.findById(materialId))
                .thenReturn(Optional.empty());

        assertTrue(service.findByMaterialId(materialId).isEmpty());
    }

    @Test
    void shouldFindTranslationKnowledgeByExactQuery() {
        UUID materialId = UUID.randomUUID();
        Material material = material(materialId);

        MaterialMatchResponse match = new MaterialMatchResponse(
                materialId,
                "Steel Pipe",
                "بايب حديد",
                MaterialMatchType.EXACT_ENGLISH_NAME,
                "Steel Pipe"
        );

        when(matchingService.findExactMatch("Steel Pipe"))
                .thenReturn(Optional.of(match));

        when(materialRepository.findById(materialId))
                .thenReturn(Optional.of(material));

        Optional<MaterialTranslationKnowledgeResponse> result =
                service.findByQuery("Steel Pipe");

        assertTrue(result.isPresent());
        assertEquals(materialId, result.get().materialId());
        assertEquals("بايب حديد", result.get().preferredIraqiName());
    }

    @Test
    void shouldResolveTranslationKnowledgeThroughApprovedAlias() {
        UUID materialId = UUID.randomUUID();
        Material material = material(materialId);

        MaterialMatchResponse match = new MaterialMatchResponse(
                materialId,
                "Steel Pipe",
                "بايب حديد",
                MaterialMatchType.APPROVED_ALIAS,
                "Iron Pipe"
        );

        when(matchingService.findExactMatch("Iron Pipe"))
                .thenReturn(Optional.of(match));

        when(materialRepository.findById(materialId))
                .thenReturn(Optional.of(material));

        Optional<MaterialTranslationKnowledgeResponse> result =
                service.findByQuery("Iron Pipe");

        assertTrue(result.isPresent());
        assertEquals(materialId, result.get().materialId());
        assertEquals("Steel Pipe", result.get().canonicalEnglishName());
        assertEquals("بايب حديد", result.get().preferredIraqiName());
    }

    @Test
    void shouldReturnEmptyWhenQueryDoesNotMatchMaterial() {
        when(matchingService.findExactMatch("Unknown"))
                .thenReturn(Optional.empty());

        assertTrue(service.findByQuery("Unknown").isEmpty());

        verify(materialRepository, never()).findById(any());
    }

    @Test
    void shouldRejectBlankQueryWithoutCallingDependencies() {
        assertTrue(service.findByQuery("   ").isEmpty());

        verifyNoInteractions(materialRepository);
        verifyNoInteractions(matchingService);
    }

    private Material material(UUID id) {
        return Material.builder()
                .id(id)
                .canonicalEnglishName("Steel Pipe")
                .preferredIraqiName("بايب حديد")
                .standardArabicName("أنبوب فولاذي")
                .unit("PCS")
                .category("Piping")
                .subCategory("Steel Pipe")
                .manufacturer("Test Manufacturer")
                .brand("Test Brand")
                .partNumber("SP-001")
                .specification("2 inch steel pipe")
                .status(MaterialStatus.APPROVED)
                .source(MaterialSource.MANUAL)
                .build();
    }
}
