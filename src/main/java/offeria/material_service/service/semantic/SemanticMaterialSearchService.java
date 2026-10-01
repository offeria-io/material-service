package offeria.material_service.service.semantic;

import lombok.RequiredArgsConstructor;
import offeria.material_service.domain.entity.Material;
import offeria.material_service.domain.enums.MaterialStatus;
import offeria.material_service.dto.response.SemanticMaterialMatchResponse;
import offeria.material_service.repository.MaterialRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SemanticMaterialSearchService {

    private final MaterialRepository materialRepository;
    private final MaterialEmbeddingService embeddingService;

    @Value("${offeria.ai.material.semantic.threshold:0.65}")
    private double threshold;

    @Value("${offeria.ai.material.semantic.limit:5}")
    private int limit;

    public List<SemanticMaterialMatchResponse> search(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException(
                    "Semantic search query must not be blank"
            );
        }

        float[] queryEmbedding =
                embeddingService.embed(query.trim());

        return materialRepository.findByStatus(MaterialStatus.APPROVED)
                .stream()
                .map(material -> score(material, queryEmbedding))
                .filter(result -> result.similarity() >= threshold)
                .sorted(
                        Comparator.comparingDouble(
                                SemanticMaterialMatchResponse::similarity
                        ).reversed()
                )
                .limit(limit)
                .toList();
    }

    private SemanticMaterialMatchResponse score(
            Material material,
            float[] queryEmbedding
    ) {
        String searchableText = buildSearchableText(material);

        float[] materialEmbedding =
                embeddingService.embed(searchableText);

        double similarity =
                embeddingService.cosineSimilarity(
                        queryEmbedding,
                        materialEmbedding
                );

        return new SemanticMaterialMatchResponse(
                material.getId(),
                material.getCanonicalEnglishName(),
                material.getPreferredIraqiName(),
                material.getStandardArabicName(),
                material.getUnit(),
                material.getCategory(),
                material.getSpecification(),
                similarity
        );
    }

    private String buildSearchableText(Material material) {
        return String.join(
                " | ",
                value(material.getCanonicalEnglishName()),
                value(material.getPreferredIraqiName()),
                value(material.getStandardArabicName()),
                value(material.getCategory()),
                value(material.getSubCategory()),
                value(material.getManufacturer()),
                value(material.getBrand()),
                value(material.getPartNumber()),
                value(material.getSpecification())
        );
    }

    private String value(String value) {
        return value == null ? "" : value.trim();
    }
}
