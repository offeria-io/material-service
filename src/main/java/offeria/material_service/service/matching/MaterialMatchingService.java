package offeria.material_service.service.matching;

import lombok.RequiredArgsConstructor;
import offeria.material_service.domain.entity.Material;
import offeria.material_service.domain.entity.MaterialAlias;
import offeria.material_service.domain.enums.MaterialMatchType;
import offeria.material_service.domain.enums.MaterialStatus;
import offeria.material_service.dto.response.MaterialMatchResponse;
import offeria.material_service.repository.MaterialAliasRepository;
import offeria.material_service.repository.MaterialRepository;
import offeria.material_service.service.normalization.MaterialNameNormalizer;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MaterialMatchingService {

    private static final int SIMILAR_RESULT_LIMIT = 10;

    private final MaterialRepository materialRepository;
    private final MaterialAliasRepository aliasRepository;
    private final MaterialNameNormalizer normalizer;

    public Optional<MaterialMatchResponse> findExactMatch(
            String value
    ) {
        String normalizedEnglish =
                normalizer.normalizeEnglish(value);

        if (normalizedEnglish == null) {
            return Optional.empty();
        }

        Optional<Material> englishMatch =
                materialRepository.findByNormalizedEnglishName(
                        normalizedEnglish
                );

        if (englishMatch.isPresent()) {
            return Optional.of(
                    MaterialMatchResponse.from(
                            englishMatch.get(),
                            MaterialMatchType.EXACT_ENGLISH_NAME,
                            value
                    )
            );
        }

        String normalizedArabic =
                normalizer.normalizeArabic(value);

        Optional<Material> iraqiMatch =
                materialRepository.findByNormalizedIraqiName(
                        normalizedArabic
                );

        if (iraqiMatch.isPresent()) {
            return Optional.of(
                    MaterialMatchResponse.from(
                            iraqiMatch.get(),
                            MaterialMatchType.EXACT_IRAQI_NAME,
                            value
                    )
            );
        }

        return aliasRepository
                .findByNormalizedAliasAndStatus(
                        normalizedEnglish,
                        MaterialStatus.APPROVED
                )
                .or(() ->
                        aliasRepository.findByNormalizedAliasAndStatus(
                                normalizedArabic,
                                MaterialStatus.APPROVED
                        )
                )
                .map(alias ->
                        MaterialMatchResponse.from(
                                alias.getMaterial(),
                                MaterialMatchType.APPROVED_ALIAS,
                                alias.getAlias()
                        )
                );
    }

    public List<MaterialMatchResponse> findSimilar(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        Map<UUID, MaterialMatchResponse> results =
                new LinkedHashMap<>();

        materialRepository.searchByName(
                        value.trim(),
                        PageRequest.of(0, SIMILAR_RESULT_LIMIT)
                )
                .forEach(material ->
                        results.putIfAbsent(
                                material.getId(),
                                MaterialMatchResponse.from(
                                        material,
                                        MaterialMatchType.SIMILAR_NAME,
                                        value
                                )
                        )
                );

        return new ArrayList<>(results.values());
    }
}
