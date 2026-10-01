package offeria.material_service.service.rfq;

import offeria.material_service.domain.enums.MaterialMatchType;
import offeria.material_service.domain.enums.MaterialSource;
import offeria.material_service.domain.enums.MaterialStatus;
import offeria.material_service.dto.response.MaterialMatchResponse;
import offeria.material_service.dto.response.MaterialTranslationKnowledgeResponse;
import offeria.material_service.dto.response.RfqMaterialResolutionResponse;
import offeria.material_service.service.matching.MaterialMatchingService;
import offeria.material_service.service.translation.MaterialTranslationKnowledgeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RfqMaterialResolutionServiceTest {

    @Mock
    private MaterialMatchingService matchingService;

    @Mock
    private MaterialTranslationKnowledgeService translationKnowledgeService;

    private RfqMaterialResolutionService service;

    @BeforeEach
    void setUp() {
        service = new RfqMaterialResolutionService(
                matchingService,
                translationKnowledgeService
        );
    }

    @Test
    void shouldResolveExactMaterialWithTranslationKnowledge() {
        UUID materialId = UUID.randomUUID();
        MaterialMatchResponse match = match(
                materialId,
                MaterialMatchType.EXACT_ENGLISH_NAME,
                "Steel Pipe"
        );

        when(matchingService.findExactMatch("Steel Pipe"))
                .thenReturn(Optional.of(match));

        when(translationKnowledgeService.findByMaterialId(materialId))
                .thenReturn(Optional.of(translation(materialId)));

        RfqMaterialResolutionResponse result =
                service.resolve("Steel Pipe");

        assertTrue(result.resolved());
        assertEquals(materialId, result.materialId());
        assertEquals("Steel Pipe", result.canonicalEnglishName());
        assertEquals("بايب حديد", result.preferredIraqiName());
        assertEquals("أنبوب فولاذي", result.standardArabicName());
        assertEquals(MaterialMatchType.EXACT_ENGLISH_NAME, result.matchType());
        assertTrue(result.similarCandidates().isEmpty());

        verify(matchingService, never()).findSimilar(anyString());
    }

    @Test
    void shouldResolveApprovedAlias() {
        UUID materialId = UUID.randomUUID();
        MaterialMatchResponse match = match(
                materialId,
                MaterialMatchType.APPROVED_ALIAS,
                "Iron Pipe"
        );

        when(matchingService.findExactMatch("Iron Pipe"))
                .thenReturn(Optional.of(match));

        when(translationKnowledgeService.findByMaterialId(materialId))
                .thenReturn(Optional.of(translation(materialId)));

        RfqMaterialResolutionResponse result =
                service.resolve("Iron Pipe");

        assertTrue(result.resolved());
        assertEquals(materialId, result.materialId());
        assertEquals(MaterialMatchType.APPROVED_ALIAS, result.matchType());
        assertEquals("بايب حديد", result.preferredIraqiName());
    }

    @Test
    void shouldReturnSimilarCandidatesWhenExactMatchDoesNotExist() {
        UUID materialId = UUID.randomUUID();

        MaterialMatchResponse similar = match(
                materialId,
                MaterialMatchType.SIMILAR_NAME,
                "Steel Pipes"
        );

        when(matchingService.findExactMatch("Steel Pip"))
                .thenReturn(Optional.empty());

        when(matchingService.findSimilar("Steel Pip"))
                .thenReturn(List.of(similar));

        RfqMaterialResolutionResponse result =
                service.resolve("Steel Pip");

        assertFalse(result.resolved());
        assertNull(result.materialId());
        assertEquals(1, result.similarCandidates().size());
        assertEquals(materialId, result.similarCandidates().getFirst().materialId());

        verifyNoInteractions(translationKnowledgeService);
    }

    @Test
    void shouldFallBackToSimilarCandidatesWhenTranslationKnowledgeIsMissing() {
        UUID materialId = UUID.randomUUID();

        MaterialMatchResponse match = match(
                materialId,
                MaterialMatchType.EXACT_ENGLISH_NAME,
                "Steel Pipe"
        );

        when(matchingService.findExactMatch("Steel Pipe"))
                .thenReturn(Optional.of(match));

        when(translationKnowledgeService.findByMaterialId(materialId))
                .thenReturn(Optional.empty());

        when(matchingService.findSimilar("Steel Pipe"))
                .thenReturn(List.of());

        RfqMaterialResolutionResponse result =
                service.resolve("Steel Pipe");

        assertFalse(result.resolved());
        assertTrue(result.similarCandidates().isEmpty());
    }

    @Test
    void shouldRejectBlankMaterialText() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.resolve("   ")
        );

        assertEquals(
                "Material text must not be blank",
                exception.getMessage()
        );

        verifyNoInteractions(matchingService);
        verifyNoInteractions(translationKnowledgeService);
    }

    @Test
    void shouldTrimMaterialTextBeforeResolution() {
        UUID materialId = UUID.randomUUID();

        MaterialMatchResponse match = match(
                materialId,
                MaterialMatchType.EXACT_ENGLISH_NAME,
                "Steel Pipe"
        );

        when(matchingService.findExactMatch("Steel Pipe"))
                .thenReturn(Optional.of(match));

        when(translationKnowledgeService.findByMaterialId(materialId))
                .thenReturn(Optional.of(translation(materialId)));

        RfqMaterialResolutionResponse result =
                service.resolve("  Steel Pipe  ");

        assertTrue(result.resolved());
        assertEquals("Steel Pipe", result.inputText());

        verify(matchingService).findExactMatch("Steel Pipe");
    }

    private MaterialMatchResponse match(
            UUID materialId,
            MaterialMatchType matchType,
            String matchedValue
    ) {
        return new MaterialMatchResponse(
                materialId,
                "Steel Pipe",
                "بايب حديد",
                matchType,
                matchedValue
        );
    }

    private MaterialTranslationKnowledgeResponse translation(UUID materialId) {
        return new MaterialTranslationKnowledgeResponse(
                materialId,
                "Steel Pipe",
                "بايب حديد",
                "أنبوب فولاذي",
                "PCS",
                "Piping",
                "Steel Pipe",
                "Test Manufacturer",
                "Test Brand",
                "SP-001",
                "2 inch steel pipe",
                MaterialStatus.APPROVED,
                MaterialSource.MANUAL
        );
    }
}
