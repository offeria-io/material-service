package offeria.material_service.service.semantic;

import offeria.material_service.domain.entity.Material;
import offeria.material_service.domain.enums.MaterialStatus;
import offeria.material_service.dto.response.SemanticMaterialMatchResponse;
import offeria.material_service.repository.MaterialRepository;
import offeria.material_service.service.matching.MaterialMatchingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class SemanticMaterialSearchServiceTest {

    private MaterialRepository repository;
    private MaterialEmbeddingService embeddingService;
    private MaterialMatchingService matchingService;
    private SemanticMaterialSearchService service;

    @BeforeEach
    void setUp() {
        repository = mock(MaterialRepository.class);
        embeddingService = mock(MaterialEmbeddingService.class);
        matchingService = mock(MaterialMatchingService.class);

        service = new SemanticMaterialSearchService(
                repository,
                embeddingService,
                matchingService
        );

        ReflectionTestUtils.setField(service, "threshold", 0.65);
        ReflectionTestUtils.setField(service, "limit", 5);
    }

    @Test
    void shouldReturnRankedApprovedMatches() {
        Material steelPipe = material(
                "Steel Pipe",
                "بايب حديد"
        );

        Material pvcPipe = material(
                "PVC Pipe",
                "بايب بلاستك"
        );

        when(repository.findByStatus(MaterialStatus.APPROVED))
                .thenReturn(List.of(steelPipe, pvcPipe));

        when(embeddingService.embed(anyString()))
                .thenReturn(new float[]{1.0f, 0.0f});

        when(embeddingService.cosineSimilarity(any(), any()))
                .thenReturn(0.91, 0.72);

        List<SemanticMaterialMatchResponse> results =
                service.search("industrial pipe");

        assertEquals(2, results.size());
        assertEquals("Steel Pipe", results.get(0).canonicalEnglishName());
        assertEquals(0.91, results.get(0).similarity(), 0.0001);

        verify(repository)
                .findByStatus(MaterialStatus.APPROVED);
    }

    @Test
    void shouldFilterBelowThreshold() {
        Material material = material(
                "Steel Pipe",
                "بايب حديد"
        );

        when(repository.findByStatus(MaterialStatus.APPROVED))
                .thenReturn(List.of(material));

        when(embeddingService.embed(anyString()))
                .thenReturn(new float[]{1.0f});

        when(embeddingService.cosineSimilarity(any(), any()))
                .thenReturn(0.40);

        assertTrue(
                service.search("unrelated material").isEmpty()
        );
    }

    @Test
    void shouldRejectBlankQuery() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.search(" ")
        );

        verifyNoInteractions(repository);
    }


    @Test
    void shouldNotUseSemanticSearchWhenExactMatchExists() {
        when(matchingService.findExactMatch("steel pipe"))
                .thenReturn(java.util.Optional.of(
                        mock(offeria.material_service.dto.response.MaterialMatchResponse.class)
                ));

        assertTrue(service.search("steel pipe").isEmpty());

        verifyNoInteractions(repository);
        verifyNoInteractions(embeddingService);
        verify(matchingService, never()).findSimilar(anyString());
    }

    @Test
    void shouldNotUseSemanticSearchWhenSimilarMatchExists() {
        when(matchingService.findExactMatch("industrial pipe"))
                .thenReturn(java.util.Optional.empty());

        when(matchingService.findSimilar("industrial pipe"))
                .thenReturn(List.of(
                        mock(offeria.material_service.dto.response.MaterialMatchResponse.class)
                ));

        assertTrue(service.search("industrial pipe").isEmpty());

        verifyNoInteractions(repository);
        verifyNoInteractions(embeddingService);
    }

    private Material material(
            String englishName,
            String iraqiName
    ) {
        return Material.builder()
                .id(UUID.randomUUID())
                .canonicalEnglishName(englishName)
                .preferredIraqiName(iraqiName)
                .unit("PCS")
                .status(MaterialStatus.APPROVED)
                .build();
    }
}
