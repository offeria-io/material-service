package offeria.material_service.ai;

import offeria.material_service.dto.response.AiMaterialSuggestionResponse;

import java.util.Optional;

public interface MaterialAiSuggestionProvider {

    Optional<AiMaterialSuggestionResponse> suggest(String materialText);
}
