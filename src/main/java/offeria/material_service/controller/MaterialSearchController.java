package offeria.material_service.controller;

import lombok.RequiredArgsConstructor;
import offeria.material_service.dto.response.MaterialMatchResponse;
import offeria.material_service.dto.response.SemanticMaterialMatchResponse;
import offeria.material_service.service.matching.MaterialMatchingService;
import offeria.material_service.service.semantic.SemanticMaterialSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/materials/search")
@RequiredArgsConstructor
public class MaterialSearchController {

    private final MaterialMatchingService matchingService;
    private final SemanticMaterialSearchService semanticSearchService;

    @GetMapping("/exact")
    public ResponseEntity<MaterialMatchResponse> findExactMatch(
            @RequestParam String query
    ) {
        if (query == null || query.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        return matchingService.findExactMatch(query)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/similar")
    public ResponseEntity<List<MaterialMatchResponse>> findSimilar(
            @RequestParam String query
    ) {
        if (query == null || query.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(
                matchingService.findSimilar(query)
        );
    }

    @GetMapping("/semantic")
    public ResponseEntity<List<SemanticMaterialMatchResponse>> findSemantic(
            @RequestParam String query
    ) {
        if (query == null || query.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(
                semanticSearchService.search(query)
        );
    }


}
