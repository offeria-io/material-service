package offeria.material_service.dto.response;

import offeria.material_service.domain.entity.Material;
import offeria.material_service.domain.enums.MaterialSource;
import offeria.material_service.domain.enums.MaterialStatus;

import java.util.UUID;

public record MaterialTranslationKnowledgeResponse(
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
        MaterialStatus status,
        MaterialSource source
) {

    public static MaterialTranslationKnowledgeResponse from(Material material) {
        return new MaterialTranslationKnowledgeResponse(
                material.getId(),
                material.getCanonicalEnglishName(),
                material.getPreferredIraqiName(),
                material.getStandardArabicName(),
                material.getUnit(),
                material.getCategory(),
                material.getSubCategory(),
                material.getManufacturer(),
                material.getBrand(),
                material.getPartNumber(),
                material.getSpecification(),
                material.getStatus(),
                material.getSource()
        );
    }
}
