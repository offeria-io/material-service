package offeria.material_service.service.mcp;

import offeria.material_service.domain.entity.Material;
import offeria.material_service.domain.enums.MaterialMatchType;
import offeria.material_service.domain.enums.MaterialStatus;
import offeria.material_service.dto.mcp.McpMaterialToolResponse;
import offeria.material_service.dto.response.MaterialMatchResponse;
import offeria.material_service.dto.response.SemanticMaterialMatchResponse;
import offeria.material_service.repository.MaterialRepository;
import offeria.material_service.service.matching.MaterialMatchingService;
import offeria.material_service.service.semantic.SemanticMaterialSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class McpMaterialToolsServiceTest {

    private MaterialRepository repository;
    private MaterialMatchingService matchingService;
    private SemanticMaterialSearchService semanticSearchService;
    private McpMaterialToolsService service;

    @BeforeEach
    void setUp() {
        repository = mock(MaterialRepository.class);
        matchingService = mock(MaterialMatchingService.class);
        semanticSearchService = mock(SemanticMaterialSearchService.class);

        service = new McpMaterialToolsService(
                repository,
                matchingService,
                semanticSearchService
        );
    }

    @Test
    void shouldGetApprovedMaterialById() {
        UUID id = UUID.randomUUID();

        Material material = Material.builder()
                .id(id)
                .canonicalEnglishName("Steel Pipe")
                .preferredIraqiName("بايب حديد")
                .unit("PCS")
                .status(MaterialStatus.APPROVED)
                .build();

        when(repository.findById(id))
                .thenReturn(Optional.of(material));

        McpMaterialToolResponse response =
                service.getMaterial(id).orElseThrow();

        assertEquals(id, response.materialId());
        assertEquals("Steel Pipe", response.canonicalEnglishName());
        assertTrue(response.approved());
    }

    @Test
    void shouldHideNonApprovedMaterial() {
        UUID id = UUID.randomUUID();

        Material material = Material.builder()
                .id(id)
                .canonicalEnglishName("Draft Material")
                .status(MaterialStatus.PENDING_REVIEW)
                .build();

        when(repository.findById(id))
                .thenReturn(Optional.of(material));

        assertTrue(service.getMaterial(id).isEmpty());
    }

    @Test
    void shouldPreferExactMatch() {
        UUID id = UUID.randomUUID();

        MaterialMatchResponse exact =
                new MaterialMatchResponse(
                        id,
                        "Steel Pipe",
                        "بايب حديد",
                        MaterialMatchType.EXACT_ENGLISH_NAME,
                        "Steel Pipe"
                );

        when(matchingService.findExactMatch("steel pipe"))
                .thenReturn(Optional.of(exact));

        McpMaterialToolResponse response =
                service.searchMaterial("steel pipe");

        assertTrue(response.resolved());
        assertEquals(id, response.materialId());
        assertEquals("EXACT_ENGLISH_NAME", response.matchType());

        verify(matchingService, never()).findSimilar(anyString());
        verifyNoInteractions(semanticSearchService);
    }

    @Test
    void shouldUseSimilarBeforeSemantic() {
        UUID id = UUID.randomUUID();

        MaterialMatchResponse similar =
                new MaterialMatchResponse(
                        id,
                        "Steel Pipe",
                        "بايب حديد",
                        MaterialMatchType.SIMILAR_NAME,
                        "Steel Pipe"
                );

        when(matchingService.findExactMatch("industrial pipe"))
                .thenReturn(Optional.empty());

        when(matchingService.findSimilar("industrial pipe"))
                .thenReturn(List.of(similar));

        McpMaterialToolResponse response =
                service.searchMaterial("industrial pipe");

        assertTrue(response.resolved());
        assertEquals(1, response.matches().size());
        assertEquals(id, response.matches().get(0).materialId());

        verifyNoInteractions(semanticSearchService);
    }

    @Test
    void shouldFallbackToSemanticSearch() {
        UUID id = UUID.randomUUID();

        when(matchingService.findExactMatch("industrial tubing"))
                .thenReturn(Optional.empty());

        when(matchingService.findSimilar("industrial tubing"))
                .thenReturn(List.of());

        when(semanticSearchService.search("industrial tubing"))
                .thenReturn(List.of(
                        new SemanticMaterialMatchResponse(
                                id,
                                "Steel Pipe",
                                "بايب حديد",
                                "أنبوب فولاذي",
                                "PCS",
                                "Piping",
                                "Carbon steel",
                                0.91
                        )
                ));

        McpMaterialToolResponse response =
                service.searchMaterial("industrial tubing");

        assertTrue(response.resolved());
        assertEquals(1, response.matches().size());
        assertEquals("SEMANTIC", response.matches().get(0).matchType());
        assertEquals(
                0.91,
                response.matches().get(0).similarity(),
                0.0001
        );
    }

    @Test
    void shouldReturnUnresolvedWhenNoMatchExists() {
        when(matchingService.findExactMatch("unknown"))
                .thenReturn(Optional.empty());

        when(matchingService.findSimilar("unknown"))
                .thenReturn(List.of());

        when(semanticSearchService.search("unknown"))
                .thenReturn(List.of());

        McpMaterialToolResponse response =
                service.searchMaterial("unknown");

        assertFalse(response.resolved());
        assertTrue(response.matches().isEmpty());
    }

    @Test
    void shouldRejectBlankQuery() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.searchMaterial(" ")
        );

        verifyNoInteractions(matchingService);
        verifyNoInteractions(semanticSearchService);
    }
}
