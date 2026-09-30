package offeria.material_service.dto.request;

import java.util.List;
import java.util.UUID;

public record LegacyMaterialBatchReviewRequest(
        List<UUID> stagingIds,
        String reviewNotes
) {
}
