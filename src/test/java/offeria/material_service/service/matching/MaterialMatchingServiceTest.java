package offeria.material_service.service.matching;

import offeria.material_service.domain.entity.Material;
import offeria.material_service.domain.entity.MaterialAlias;
import offeria.material_service.domain.enums.MaterialMatchType;
import offeria.material_service.domain.enums.MaterialStatus;
import offeria.material_service.dto.response.MaterialMatchResponse;
import offeria.material_service.repository.MaterialAliasRepository;
import offeria.material_service.repository.MaterialRepository;
import offeria.material_service.service.normalization.MaterialNameNormalizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaterialMatchingServiceTest {

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private MaterialAliasRepository aliasRepository;

    private MaterialMatchingService matchingService;

    @BeforeEach
    void setUp() {
        matchingService = new MaterialMatchingService(
                materialRepository,
                aliasRepository,
                new MaterialNameNormalizer()
        );
    }

    @Test
    void shouldFindExactEnglishMatch() {
        Material material = material(
                "Steel Pipe",
                "بايب حديد"
        );

        when(materialRepository.findByNormalizedEnglishName(
                "steel pipe"
        )).thenReturn(Optional.of(material));

        Optional<MaterialMatchResponse> result =
                matchingService.findExactMatch("  STEEL   PIPE ");

        assertTrue(result.isPresent());
        assertEquals(
                MaterialMatchType.EXACT_ENGLISH_NAME,
                result.get().matchType()
        );
        assertEquals(material.getId(), result.get().materialId());

        verifyNoInteractions(aliasRepository);
    }

    @Test
    void shouldFindExactIraqiMatch() {
        Material material = material(
                "Steel Pipe",
                "بايب حديد"
        );

        when(materialRepository.findByNormalizedEnglishName(
                "بايب حديد"
        )).thenReturn(Optional.empty());

        when(materialRepository.findByNormalizedIraqiName(
                "بايب حديد"
        )).thenReturn(Optional.of(material));

        Optional<MaterialMatchResponse> result =
                matchingService.findExactMatch("  بايب   حديد ");

        assertTrue(result.isPresent());
        assertEquals(
                MaterialMatchType.EXACT_IRAQI_NAME,
                result.get().matchType()
        );
    }

    @Test
    void shouldFindApprovedAliasMatch() {
        Material material = material(
                "Steel Pipe",
                "بايب حديد"
        );

        MaterialAlias alias = MaterialAlias.builder()
                .id(UUID.randomUUID())
                .material(material)
                .alias("Pipe Steel")
                .normalizedAlias("pipe steel")
                .status(MaterialStatus.APPROVED)
                .build();

        when(materialRepository.findByNormalizedEnglishName(
                "pipe steel"
        )).thenReturn(Optional.empty());

        when(materialRepository.findByNormalizedIraqiName(
                "Pipe Steel"
        )).thenReturn(Optional.empty());

        when(aliasRepository.findByNormalizedAliasAndStatus(
                "pipe steel",
                MaterialStatus.APPROVED
        )).thenReturn(Optional.of(alias));

        Optional<MaterialMatchResponse> result =
                matchingService.findExactMatch("Pipe Steel");

        assertTrue(result.isPresent());
        assertEquals(
                MaterialMatchType.APPROVED_ALIAS,
                result.get().matchType()
        );
        assertEquals("Pipe Steel", result.get().matchedValue());
    }

    @Test
    void shouldNotMatchUnapprovedAlias() {
        when(materialRepository.findByNormalizedEnglishName(
                "old pipe"
        )).thenReturn(Optional.empty());

        when(materialRepository.findByNormalizedIraqiName(
                "Old Pipe"
        )).thenReturn(Optional.empty());

        when(aliasRepository.findByNormalizedAliasAndStatus(
                "old pipe",
                MaterialStatus.APPROVED
        )).thenReturn(Optional.empty());

        when(aliasRepository.findByNormalizedAliasAndStatus(
                "Old Pipe",
                MaterialStatus.APPROVED
        )).thenReturn(Optional.empty());

        Optional<MaterialMatchResponse> result =
                matchingService.findExactMatch("Old Pipe");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnSimilarMaterials() {
        Material first = material(
                "Steel Pipe 2 Inch",
                "بايب حديد 2 انج"
        );

        Material second = material(
                "Steel Pipe 4 Inch",
                "بايب حديد 4 انج"
        );

        when(materialRepository.searchByName(
                eq("Steel Pipe"),
                any(Pageable.class)
        )).thenReturn(
                new PageImpl<>(List.of(first, second))
        );

        List<MaterialMatchResponse> results =
                matchingService.findSimilar("Steel Pipe");

        assertEquals(2, results.size());

        assertTrue(
                results.stream().allMatch(
                        result ->
                                result.matchType()
                                        == MaterialMatchType.SIMILAR_NAME
                )
        );
    }

    @Test
    void shouldReturnEmptyWhenNoMatchExists() {
        when(materialRepository.findByNormalizedEnglishName(
                "unknown material"
        )).thenReturn(Optional.empty());

        when(materialRepository.findByNormalizedIraqiName(
                "Unknown Material"
        )).thenReturn(Optional.empty());

        when(aliasRepository.findByNormalizedAliasAndStatus(
                "unknown material",
                MaterialStatus.APPROVED
        )).thenReturn(Optional.empty());

        when(aliasRepository.findByNormalizedAliasAndStatus(
                "Unknown Material",
                MaterialStatus.APPROVED
        )).thenReturn(Optional.empty());

        Optional<MaterialMatchResponse> result =
                matchingService.findExactMatch(
                        "Unknown Material"
                );

        assertTrue(result.isEmpty());
    }

    private Material material(
            String englishName,
            String iraqiName
    ) {
        return Material.builder()
                .id(UUID.randomUUID())
                .canonicalEnglishName(englishName)
                .preferredIraqiName(iraqiName)
                .normalizedEnglishName(
                        englishName.toLowerCase()
                )
                .normalizedIraqiName(iraqiName)
                .status(MaterialStatus.APPROVED)
                .build();
    }
}
