package offeria.material_service.service.importing;

import offeria.material_service.domain.entity.Material;
import offeria.material_service.dto.response.LegacyMaterialBatchItemResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LegacyMaterialBatchServiceTest {

    @Mock
    private LegacyMaterialReviewService reviewService;

    @Mock
    private LegacyMaterialPromotionService promotionService;

    private LegacyMaterialBatchService batchService;

    @BeforeEach
    void setUp() {
        batchService = new LegacyMaterialBatchService(
                reviewService,
                promotionService
        );
    }

    @Test
    void shouldApproveSelectedRecords() {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();

        List<LegacyMaterialBatchItemResponse> results =
                batchService.approve(
                        List.of(firstId, secondId),
                        "Reviewed"
                );

        assertEquals(2, results.size());
        assertTrue(results.get(0).success());
        assertTrue(results.get(1).success());

        verify(reviewService).approve(firstId, "Reviewed");
        verify(reviewService).approve(secondId, "Reviewed");
    }

    @Test
    void shouldReportMixedApproveResults() {
        UUID validId = UUID.randomUUID();
        UUID invalidId = UUID.randomUUID();

        when(reviewService.approve(validId, "Reviewed"))
                .thenReturn(
                        offeria.material_service.domain.entity.LegacyMaterialStaging.builder()
                                .id(validId)
                                .build()
                );

        when(reviewService.approve(invalidId, "Reviewed"))
                .thenThrow(new IllegalStateException(
                        "Only PENDING_REVIEW staging records can be reviewed"
                ));

        List<LegacyMaterialBatchItemResponse> results =
                batchService.approve(
                        List.of(validId, invalidId),
                        "Reviewed"
                );

        assertTrue(results.get(0).success());
        assertFalse(results.get(1).success());
        assertEquals(
                "Only PENDING_REVIEW staging records can be reviewed",
                results.get(1).message()
        );
    }

    @Test
    void shouldRejectSelectedRecords() {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();

        List<LegacyMaterialBatchItemResponse> results =
                batchService.reject(
                        List.of(firstId, secondId),
                        "Rejected after review"
                );

        assertEquals(2, results.size());
        assertTrue(results.get(0).success());
        assertTrue(results.get(1).success());

        verify(reviewService).reject(
                firstId,
                "Rejected after review"
        );
        verify(reviewService).reject(
                secondId,
                "Rejected after review"
        );
    }

    @Test
    void shouldReportMixedPromotionResults() {
        UUID validId = UUID.randomUUID();
        UUID invalidId = UUID.randomUUID();
        UUID materialId = UUID.randomUUID();

        Material material = Material.builder()
                .id(materialId)
                .build();

        when(promotionService.promote(validId))
                .thenReturn(material);

        when(promotionService.promote(invalidId))
                .thenThrow(new IllegalStateException(
                        "Only APPROVED_FOR_IMPORT staging records can be promoted"
                ));

        List<LegacyMaterialBatchItemResponse> results =
                batchService.promote(
                        List.of(validId, invalidId)
                );

        assertEquals(2, results.size());

        assertTrue(results.get(0).success());
        assertEquals(
                materialId,
                results.get(0).materialId()
        );

        assertFalse(results.get(1).success());
        assertNull(results.get(1).materialId());
        assertEquals(
                "Only APPROVED_FOR_IMPORT staging records can be promoted",
                results.get(1).message()
        );
    }

    @Test
    void shouldRejectEmptyBatch() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> batchService.approve(
                                List.of(),
                                "Reviewed"
                        )
                );

        assertEquals(
                "stagingIds must not be empty",
                exception.getMessage()
        );

        verifyNoInteractions(reviewService);
    }
}
