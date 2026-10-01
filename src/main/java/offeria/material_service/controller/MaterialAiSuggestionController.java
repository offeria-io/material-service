package offeria.material_service.controller;

import lombok.RequiredArgsConstructor;
import offeria.material_service.dto.request.AiMaterialSuggestionRequest;
import offeria.material_service.dto.response.AiMaterialSuggestionResponse;
import offeria.material_service.service.ai.MaterialAiSuggestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/materials/ai")
@RequiredArgsConstructor
public class MaterialAiSuggestionController {

    private final MaterialAiSuggestionService suggestionService;

    @PostMapping("/suggest")
    public ResponseEntity<AiMaterialSuggestionResponse> suggest(
            @RequestBody AiMaterialSuggestionRequest request
    ) {
        if (request == null
                || request.materialText() == null
                || request.materialText().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        return suggestionService.suggest(request.materialText())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
