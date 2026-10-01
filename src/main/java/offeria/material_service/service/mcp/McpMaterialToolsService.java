package offeria.material_service.service.mcp;

import lombok.RequiredArgsConstructor;
import offeria.material_service.domain.entity.Material;
import offeria.material_service.domain.enums.MaterialStatus;
import offeria.material_service.dto.mcp.McpMaterialToolResponse;
import offeria.material_service.dto.mcp.McpMaterialTranslationResponse;
import offeria.material_service.dto.mcp.McpMaterialSuggestionResponse;
import offeria.material_service.dto.response.AiMaterialSuggestionResponse;
import offeria.material_service.dto.response.MaterialTranslationKnowledgeResponse;
import offeria.material_service.dto.response.MaterialMatchResponse;
import offeria.material_service.dto.response.SemanticMaterialMatchResponse;
import offeria.material_service.repository.MaterialRepository;
import offeria.material_service.service.matching.MaterialMatchingService;
import offeria.material_service.service.semantic.SemanticMaterialSearchService;
import offeria.material_service.service.translation.MaterialTranslationKnowledgeService;
import offeria.material_service.service.ai.MaterialAiSuggestionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class McpMaterialToolsService {

    private final MaterialRepository materialRepository;
    private final MaterialMatchingService matchingService;
    private final SemanticMaterialSearchService semanticSearchService;
    private final MaterialTranslationKnowledgeService translationKnowledgeService;
    private final MaterialAiSuggestionService aiSuggestionService;

    public Optional<McpMaterialToolResponse> getMaterial(UUID materialId) {
        if (materialId == null) {
            return Optional.empty();
        }

        return materialRepository.findById(materialId)
                .filter(material ->
                        material.getStatus() == MaterialStatus.APPROVED
                )
                .map(material ->
                        fromMaterial(
                                "get_material",
                                material.getCanonicalEnglishName(),
                                material
                        )
                );
    }

    public McpMaterialToolResponse searchMaterial(String query) {
        String value = requireQuery(query);

        Optional<MaterialMatchResponse> exact =
                matchingService.findExactMatch(value);

        if (exact.isPresent()) {
            return fromMatch(
                    "search_material",
                    value,
                    exact.get()
            );
        }

        List<MaterialMatchResponse> similar =
                matchingService.findSimilar(value);

        if (!similar.isEmpty()) {
            List<McpMaterialToolResponse> matches =
                    similar.stream()
                            .map(match ->
                                    fromMatch(
                                            "search_material",
                                            value,
                                            match
                                    )
                            )
                            .toList();

            return aggregate(
                    "search_material",
                    value,
                    matches
            );
        }

        List<SemanticMaterialMatchResponse> semantic =
                semanticSearchService.search(value);

        if (!semantic.isEmpty()) {
            List<McpMaterialToolResponse> matches =
                    semantic.stream()
                            .map(match ->
                                    fromSemantic(
                                            "search_material",
                                            value,
                                            match
                                    )
                            )
                            .toList();

            return aggregate(
                    "search_material",
                    value,
                    matches
            );
        }

        return McpMaterialToolResponse.unresolved(
                "search_material",
                value
        );
    }


    public McpMaterialToolResponse findSimilarMaterial(String query) {
        String value = requireQuery(query);

        List<MaterialMatchResponse> similar =
                matchingService.findSimilar(value);

        if (!similar.isEmpty()) {
            return aggregate(
                    "find_similar_material",
                    value,
                    similar.stream()
                            .map(match -> fromMatch(
                                    "find_similar_material",
                                    value,
                                    match
                            ))
                            .toList()
            );
        }

        List<SemanticMaterialMatchResponse> semantic =
                semanticSearchService.search(value);

        if (!semantic.isEmpty()) {
            return aggregate(
                    "find_similar_material",
                    value,
                    semantic.stream()
                            .map(match -> fromSemantic(
                                    "find_similar_material",
                                    value,
                                    match
                            ))
                            .toList()
            );
        }

        return McpMaterialToolResponse.unresolved(
                "find_similar_material",
                value
        );
    }

    public Optional<McpMaterialTranslationResponse> getMaterialTranslation(
            UUID materialId
    ) {
        if (materialId == null) {
            return Optional.empty();
        }

        return translationKnowledgeService
                .findByMaterialId(materialId)
                .filter(response ->
                        response.status() == MaterialStatus.APPROVED
                )
                .map(this::fromTranslation);
    }

    public Optional<McpMaterialSuggestionResponse> suggestMaterialTranslation(
            String query
    ) {
        String value = requireQuery(query);

        return aiSuggestionService
                .suggest(value)
                .map(this::fromSuggestion);
    }

    private McpMaterialTranslationResponse fromTranslation(
            MaterialTranslationKnowledgeResponse response
    ) {
        return new McpMaterialTranslationResponse(
                response.materialId(),
                response.canonicalEnglishName(),
                response.preferredIraqiName(),
                response.standardArabicName(),
                response.unit(),
                response.category(),
                response.subCategory(),
                response.manufacturer(),
                response.brand(),
                response.partNumber(),
                response.specification(),
                true
        );
    }

    private McpMaterialSuggestionResponse fromSuggestion(
            AiMaterialSuggestionResponse response
    ) {
        return new McpMaterialSuggestionResponse(
                response.inputText(),
                response.canonicalEnglishName(),
                response.preferredIraqiName(),
                response.standardArabicName(),
                response.unit(),
                response.category(),
                response.specification(),
                false
        );
    }

    private McpMaterialToolResponse fromMaterial(
            String tool,
            String query,
            Material material
    ) {
        return new McpMaterialToolResponse(
                tool,
                query,
                true,
                material.getId(),
                material.getCanonicalEnglishName(),
                material.getPreferredIraqiName(),
                material.getStandardArabicName(),
                material.getUnit(),
                material.getCategory(),
                material.getSpecification(),
                "MATERIAL_ID",
                null,
                true,
                List.of()
        );
    }

    private McpMaterialToolResponse fromMatch(
            String tool,
            String query,
            MaterialMatchResponse match
    ) {
        return new McpMaterialToolResponse(
                tool,
                query,
                true,
                match.materialId(),
                match.canonicalEnglishName(),
                match.preferredIraqiName(),
                null,
                null,
                null,
                null,
                match.matchType().name(),
                null,
                true,
                List.of()
        );
    }

    private McpMaterialToolResponse fromSemantic(
            String tool,
            String query,
            SemanticMaterialMatchResponse match
    ) {
        return new McpMaterialToolResponse(
                tool,
                query,
                true,
                match.materialId(),
                match.canonicalEnglishName(),
                match.preferredIraqiName(),
                match.standardArabicName(),
                match.unit(),
                match.category(),
                match.specification(),
                "SEMANTIC",
                match.similarity(),
                true,
                List.of()
        );
    }

    private McpMaterialToolResponse aggregate(
            String tool,
            String query,
            List<McpMaterialToolResponse> matches
    ) {
        return new McpMaterialToolResponse(
                tool,
                query,
                !matches.isEmpty(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "MULTIPLE",
                null,
                true,
                matches
        );
    }

    private String requireQuery(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException(
                    "Material query must not be blank"
            );
        }

        return query.trim();
    }
}
