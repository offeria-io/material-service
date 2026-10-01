package offeria.material_service.dto.response;

import java.util.UUID;

public record AiMaterialApprovalResponse(
        UUID materialId,
        String canonicalEnglishName,
        String preferredIraqiName,
        String standardArabicName,
        String unit,
        String category,
        String specification,
        String status,
        String source
) {
}
