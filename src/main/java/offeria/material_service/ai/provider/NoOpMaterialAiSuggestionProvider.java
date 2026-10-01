package offeria.material_service.ai.provider;

import offeria.material_service.ai.MaterialAiSuggestionProvider;
import offeria.material_service.dto.response.AiMaterialSuggestionResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@ConditionalOnProperty(
        name = "offeria.ai.material.provider",
        havingValue = "noop",
        matchIfMissing = true
)
public class NoOpMaterialAiSuggestionProvider implements MaterialAiSuggestionProvider {

    @Override
    public Optional<AiMaterialSuggestionResponse> suggest(String materialText) {
        return Optional.empty();
    }
}
