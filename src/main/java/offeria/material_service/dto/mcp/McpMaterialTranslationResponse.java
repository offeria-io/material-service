package offeria.material_service.dto.mcp;

import java.util.UUID;

public record McpMaterialTranslationResponse(
        UUID materialId,
        String canonicalEnglishName,
        String preferredIraqiName,
        String standardArabicName,
        String unit,
        String category,
        String subCategory,
        String manufacturer,
        String brand,
        String partNumber,
        String specification,
        boolean approved
) {
}
