package offeria.material_service.dto.request;

import java.util.List;
import java.util.UUID;

public record LegacyMaterialBatchPromotionRequest(
        List<UUID> stagingIds
) {
}
