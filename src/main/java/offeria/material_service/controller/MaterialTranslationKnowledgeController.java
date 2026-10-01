package offeria.material_service.controller;

import lombok.RequiredArgsConstructor;
import offeria.material_service.dto.response.MaterialTranslationKnowledgeResponse;
import offeria.material_service.service.translation.MaterialTranslationKnowledgeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/materials/translations")
@RequiredArgsConstructor
public class MaterialTranslationKnowledgeController {

    private final MaterialTranslationKnowledgeService translationKnowledgeService;

    @GetMapping("/{materialId}")
    public ResponseEntity<MaterialTranslationKnowledgeResponse> findByMaterialId(
            @PathVariable UUID materialId
    ) {
        return translationKnowledgeService.findByMaterialId(materialId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<MaterialTranslationKnowledgeResponse> findByQuery(
            @RequestParam String query
    ) {
        if (query == null || query.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        return translationKnowledgeService.findByQuery(query)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
