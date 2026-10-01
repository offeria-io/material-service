package offeria.material_service.service.ai;

import lombok.RequiredArgsConstructor;
import offeria.material_service.ai.MaterialAiSuggestionProvider;
import offeria.material_service.dto.response.AiMaterialSuggestionResponse;
import offeria.material_service.service.matching.MaterialMatchingService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MaterialAiSuggestionService {

    private final MaterialMatchingService matchingService;
    private final MaterialAiSuggestionProvider aiSuggestionProvider;

    public Optional<AiMaterialSuggestionResponse> suggest(String materialText) {
        if (materialText == null || materialText.isBlank()) {
            throw new IllegalArgumentException("Material text must not be blank");
        }

        String query = materialText.trim();

        if (matchingService.findExactMatch(query).isPresent()) {
            return Optional.empty();
        }

        return aiSuggestionProvider.suggest(query)
                .map(suggestion -> AiMaterialSuggestionResponse.unapproved(
                        query,
                        suggestion.canonicalEnglishName(),
                        suggestion.preferredIraqiName(),
                        suggestion.standardArabicName(),
                        suggestion.unit(),
                        suggestion.category(),
                        suggestion.specification()
                ));
    }
}
