package offeria.material_service.dto.response;

import java.util.UUID;

public record SemanticMaterialMatchResponse(
        UUID materialId,
        String canonicalEnglishName,
        String preferredIraqiName,
        String standardArabicName,
        String unit,
        String category,
        String specification,
        double similarity
) {
}
