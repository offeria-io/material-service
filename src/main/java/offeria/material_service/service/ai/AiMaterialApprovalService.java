package offeria.material_service.service.ai;

import lombok.RequiredArgsConstructor;
import offeria.material_service.domain.entity.Material;
import offeria.material_service.domain.enums.MaterialSource;
import offeria.material_service.domain.enums.MaterialStatus;
import offeria.material_service.dto.request.AiMaterialApprovalRequest;
import offeria.material_service.dto.response.AiMaterialApprovalResponse;
import offeria.material_service.repository.MaterialRepository;
import offeria.material_service.service.normalization.MaterialNameNormalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiMaterialApprovalService {

    private final MaterialRepository materialRepository;
    private final MaterialNameNormalizer materialNameNormalizer;

    @Transactional
    public AiMaterialApprovalResponse approve(AiMaterialApprovalRequest request) {
        validate(request);

        String canonicalName = request.canonicalEnglishName().trim();
        String unit = request.unit().trim();

        String normalizedEnglish =
                materialNameNormalizer.normalizeEnglish(canonicalName);

        if (materialRepository.findByNormalizedEnglishName(normalizedEnglish).isPresent()) {
            throw new IllegalStateException(
                    "Material already exists: " + canonicalName
            );
        }

        String iraqiName = trimToNull(request.preferredIraqiName());

        String normalizedIraqi = iraqiName == null
                ? null
                : materialNameNormalizer.normalizeArabic(iraqiName);

        Material material = Material.builder()
                .canonicalEnglishName(canonicalName)
                .preferredIraqiName(iraqiName)
                .standardArabicName(trimToNull(request.standardArabicName()))
                .normalizedEnglishName(normalizedEnglish)
                .normalizedIraqiName(normalizedIraqi)
                .unit(unit)
                .category(trimToNull(request.category()))
                .specification(trimToNull(request.specification()))
                .status(MaterialStatus.APPROVED)
                .source(MaterialSource.AI_SUGGESTED)
                .build();

        Material saved = materialRepository.save(material);

        return new AiMaterialApprovalResponse(
                saved.getId(),
                saved.getCanonicalEnglishName(),
                saved.getPreferredIraqiName(),
                saved.getStandardArabicName(),
                saved.getUnit(),
                saved.getCategory(),
                saved.getSpecification(),
                saved.getStatus().name(),
                saved.getSource().name()
        );
    }

    private void validate(AiMaterialApprovalRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Approval request must not be null");
        }

        if (request.canonicalEnglishName() == null
                || request.canonicalEnglishName().isBlank()) {
            throw new IllegalArgumentException(
                    "Canonical English name must not be blank"
            );
        }

        if (request.unit() == null || request.unit().isBlank()) {
            throw new IllegalArgumentException("Unit must not be blank");
        }
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
