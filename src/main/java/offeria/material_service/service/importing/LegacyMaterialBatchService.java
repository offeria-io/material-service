package offeria.material_service.service.importing;

import lombok.RequiredArgsConstructor;
import offeria.material_service.domain.entity.Material;
import offeria.material_service.dto.response.LegacyMaterialBatchItemResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class LegacyMaterialBatchService {

    private final LegacyMaterialReviewService reviewService;
    private final LegacyMaterialPromotionService promotionService;

    public List<LegacyMaterialBatchItemResponse> approve(
            List<UUID> stagingIds,
            String reviewNotes
    ) {
        return review(
                stagingIds,
                id -> reviewService.approve(id, reviewNotes)
        );
    }

    public List<LegacyMaterialBatchItemResponse> reject(
            List<UUID> stagingIds,
            String reviewNotes
    ) {
        return review(
                stagingIds,
                id -> reviewService.reject(id, reviewNotes)
        );
    }

    public List<LegacyMaterialBatchItemResponse> promote(
            List<UUID> stagingIds
    ) {
        validateIds(stagingIds);

        return stagingIds.stream()
                .map(this::promoteOne)
                .toList();
    }

    private List<LegacyMaterialBatchItemResponse> review(
            List<UUID> stagingIds,
            Consumer<UUID> operation
    ) {
        validateIds(stagingIds);

        return stagingIds.stream()
                .map(id -> reviewOne(id, operation))
                .toList();
    }

    private LegacyMaterialBatchItemResponse reviewOne(
            UUID stagingId,
            Consumer<UUID> operation
    ) {
        try {
            operation.accept(stagingId);

            return LegacyMaterialBatchItemResponse.success(
                    stagingId
            );
        } catch (RuntimeException ex) {
            return LegacyMaterialBatchItemResponse.failure(
                    stagingId,
                    ex.getMessage()
            );
        }
    }

    private LegacyMaterialBatchItemResponse promoteOne(
            UUID stagingId
    ) {
        try {
            Material material =
                    promotionService.promote(stagingId);

            return LegacyMaterialBatchItemResponse.promoted(
                    stagingId,
                    material.getId()
            );
        } catch (RuntimeException ex) {
            return LegacyMaterialBatchItemResponse.failure(
                    stagingId,
                    ex.getMessage()
            );
        }
    }

    private void validateIds(List<UUID> stagingIds) {
        if (stagingIds == null || stagingIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "stagingIds must not be empty"
            );
        }

        if (stagingIds.stream().anyMatch(id -> id == null)) {
            throw new IllegalArgumentException(
                    "stagingIds must not contain null values"
            );
        }
    }
}
