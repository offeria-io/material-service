package offeria.material_service.service.rfq;

import lombok.RequiredArgsConstructor;
import offeria.material_service.dto.response.MaterialMatchResponse;
import offeria.material_service.dto.response.RfqMaterialResolutionResponse;
import offeria.material_service.service.matching.MaterialMatchingService;
import offeria.material_service.service.translation.MaterialTranslationKnowledgeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RfqMaterialResolutionService {

    private final MaterialMatchingService matchingService;
    private final MaterialTranslationKnowledgeService translationKnowledgeService;

    public RfqMaterialResolutionResponse resolve(String materialText) {
        if (materialText == null || materialText.isBlank()) {
            throw new IllegalArgumentException("Material text must not be blank");
        }

        String query = materialText.trim();

        return matchingService.findExactMatch(query)
                .flatMap(match ->
                        translationKnowledgeService.findByMaterialId(match.materialId())
                                .map(translation ->
                                        RfqMaterialResolutionResponse.resolved(
                                                query,
                                                match,
                                                translation
                                        )
                                )
                )
                .orElseGet(() -> {
                    List<MaterialMatchResponse> similarCandidates =
                            matchingService.findSimilar(query);

                    return RfqMaterialResolutionResponse.unresolved(
                            query,
                            similarCandidates
                    );
                });
    }
}
