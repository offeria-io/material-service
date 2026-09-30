package offeria.material_service.dto.response;

import offeria.material_service.domain.entity.Material;
import offeria.material_service.domain.enums.MaterialMatchType;

import java.util.UUID;

public record MaterialMatchResponse(
        UUID materialId,
        String canonicalEnglishName,
        String preferredIraqiName,
        MaterialMatchType matchType,
        String matchedValue
) {

    public static MaterialMatchResponse from(
            Material material,
            MaterialMatchType matchType,
            String matchedValue
    ) {
        return new MaterialMatchResponse(
                material.getId(),
                material.getCanonicalEnglishName(),
                material.getPreferredIraqiName(),
                matchType,
                matchedValue
        );
    }
}
