package offeria.material_service.service.ai;

import offeria.material_service.domain.entity.Material;
import offeria.material_service.domain.enums.MaterialSource;
import offeria.material_service.domain.enums.MaterialStatus;
import offeria.material_service.dto.request.AiMaterialApprovalRequest;
import offeria.material_service.dto.response.AiMaterialApprovalResponse;
import offeria.material_service.repository.MaterialRepository;
import offeria.material_service.service.normalization.MaterialNameNormalizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiMaterialApprovalServiceTest {

    @Mock
    private MaterialRepository materialRepository;

    private AiMaterialApprovalService service;

    @BeforeEach
    void setUp() {
        service = new AiMaterialApprovalService(
                materialRepository,
                new MaterialNameNormalizer()
        );
    }

    @Test
    void shouldApproveAndPersistAiSuggestion() {
        AiMaterialApprovalRequest request = new AiMaterialApprovalRequest(
                "Steel Pipe",
                "بايب حديد",
                "أنبوب فولاذي",
                "M",
                "Piping",
                "Carbon steel"
        );

        when(materialRepository.findByNormalizedEnglishName("steel pipe"))
                .thenReturn(Optional.empty());

        when(materialRepository.save(any(Material.class)))
                .thenAnswer(invocation -> {
                    Material material = invocation.getArgument(0);
                    material.setId(UUID.randomUUID());
                    return material;
                });

        AiMaterialApprovalResponse response = service.approve(request);

        assertNotNull(response.materialId());
        assertEquals("Steel Pipe", response.canonicalEnglishName());
        assertEquals("APPROVED", response.status());
        assertEquals("AI_SUGGESTED", response.source());

        verify(materialRepository).save(argThat(material ->
                material.getStatus() == MaterialStatus.APPROVED
                        && material.getSource() == MaterialSource.AI_SUGGESTED
                        && "steel pipe".equals(material.getNormalizedEnglishName())
                        && "بايب حديد".equals(material.getNormalizedIraqiName())
        ));
    }

    @Test
    void shouldRejectDuplicateMaterial() {
        AiMaterialApprovalRequest request = new AiMaterialApprovalRequest(
                "Steel Pipe",
                "بايب حديد",
                "أنبوب فولاذي",
                "M",
                "Piping",
                null
        );

        when(materialRepository.findByNormalizedEnglishName("steel pipe"))
                .thenReturn(Optional.of(Material.builder().build()));

        assertThrows(
                IllegalStateException.class,
                () -> service.approve(request)
        );

        verify(materialRepository, never()).save(any());
    }

    @Test
    void shouldRejectBlankCanonicalEnglishName() {
        AiMaterialApprovalRequest request = new AiMaterialApprovalRequest(
                "   ",
                null,
                null,
                "PCS",
                null,
                null
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.approve(request)
        );

        verifyNoInteractions(materialRepository);
    }

    @Test
    void shouldRejectBlankUnit() {
        AiMaterialApprovalRequest request = new AiMaterialApprovalRequest(
                "Steel Pipe",
                null,
                null,
                " ",
                null,
                null
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.approve(request)
        );

        verifyNoInteractions(materialRepository);
    }

    @Test
    void shouldRejectNullRequest() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.approve(null)
        );

        verifyNoInteractions(materialRepository);
    }

    @Test
    void shouldTrimValuesBeforePersistence() {
        AiMaterialApprovalRequest request = new AiMaterialApprovalRequest(
                "  Steel Pipe  ",
                "  بايب حديد  ",
                "  أنبوب فولاذي  ",
                "  M  ",
                "  Piping  ",
                "  Carbon steel  "
        );

        when(materialRepository.findByNormalizedEnglishName("steel pipe"))
                .thenReturn(Optional.empty());

        when(materialRepository.save(any(Material.class)))
                .thenAnswer(invocation -> {
                    Material material = invocation.getArgument(0);
                    material.setId(UUID.randomUUID());
                    return material;
                });

        service.approve(request);

        verify(materialRepository).save(argThat(material ->
                "Steel Pipe".equals(material.getCanonicalEnglishName())
                        && "بايب حديد".equals(material.getPreferredIraqiName())
                        && "أنبوب فولاذي".equals(material.getStandardArabicName())
                        && "M".equals(material.getUnit())
                        && "Piping".equals(material.getCategory())
                        && "Carbon steel".equals(material.getSpecification())
        ));
    }
}
