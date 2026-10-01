package offeria.material_service.dto.response;

import offeria.material_service.domain.enums.MaterialMatchType;

import java.util.List;
import java.util.UUID;

public record RfqMaterialResolutionResponse(
        String inputText,
        boolean resolved,
        UUID materialId,
        String canonicalEnglishName,
        String preferredIraqiName,
        String standardArabicName,
        MaterialMatchType matchType,
        List<MaterialMatchResponse> similarCandidates
) {

    public static RfqMaterialResolutionResponse resolved(
            String inputText,
            MaterialMatchResponse match,
            MaterialTranslationKnowledgeResponse translation
    ) {
        return new RfqMaterialResolutionResponse(
                inputText,
                true,
                match.materialId(),
                translation.canonicalEnglishName(),
                translation.preferredIraqiName(),
                translation.standardArabicName(),
                match.matchType(),
                List.of()
        );
    }

    public static RfqMaterialResolutionResponse unresolved(
            String inputText,
            List<MaterialMatchResponse> similarCandidates
    ) {
        return new RfqMaterialResolutionResponse(
                inputText,
                false,
                null,
                null,
                null,
                null,
                null,
                similarCandidates
        );
    }
}
