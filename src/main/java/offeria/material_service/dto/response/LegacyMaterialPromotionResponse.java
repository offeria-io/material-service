package offeria.material_service.dto.response;

import offeria.material_service.domain.entity.Material;
import offeria.material_service.domain.enums.MaterialSource;
import offeria.material_service.domain.enums.MaterialStatus;

import java.util.UUID;

public record LegacyMaterialPromotionResponse(
        UUID materialId,
        String canonicalEnglishName,
        String preferredIraqiName,
        String standardArabicName,
        String unit,
        MaterialStatus status,
        MaterialSource source
) {

    public static LegacyMaterialPromotionResponse from(
            Material material
    ) {
        return new LegacyMaterialPromotionResponse(
                material.getId(),
                material.getCanonicalEnglishName(),
                material.getPreferredIraqiName(),
                material.getStandardArabicName(),
                material.getUnit(),
                material.getStatus(),
                material.getSource()
        );
    }
}
