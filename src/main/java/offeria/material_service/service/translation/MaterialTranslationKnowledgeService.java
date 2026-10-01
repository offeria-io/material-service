package offeria.material_service.service.translation;

import lombok.RequiredArgsConstructor;
import offeria.material_service.domain.entity.Material;
import offeria.material_service.dto.response.MaterialMatchResponse;
import offeria.material_service.dto.response.MaterialTranslationKnowledgeResponse;
import offeria.material_service.repository.MaterialRepository;
import offeria.material_service.service.matching.MaterialMatchingService;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MaterialTranslationKnowledgeService {

    private final MaterialRepository materialRepository;
    private final MaterialMatchingService matchingService;

    public Optional<MaterialTranslationKnowledgeResponse> findByMaterialId(UUID materialId) {
        if (materialId == null) {
            return Optional.empty();
        }

        return materialRepository.findById(materialId)
                .map(MaterialTranslationKnowledgeResponse::from);
    }

    public Optional<MaterialTranslationKnowledgeResponse> findByQuery(String query) {
        if (query == null || query.isBlank()) {
            return Optional.empty();
        }

        return matchingService.findExactMatch(query)
                .map(MaterialMatchResponse::materialId)
                .flatMap(materialRepository::findById)
                .map(MaterialTranslationKnowledgeResponse::from);
    }
}
