package offeria.material_service.controller;

import lombok.RequiredArgsConstructor;
import offeria.material_service.dto.request.RfqMaterialResolutionRequest;
import offeria.material_service.dto.response.RfqMaterialResolutionResponse;
import offeria.material_service.service.rfq.RfqMaterialResolutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/materials/rfq")
@RequiredArgsConstructor
public class RfqMaterialResolutionController {

    private final RfqMaterialResolutionService resolutionService;

    @PostMapping("/resolve")
    public ResponseEntity<RfqMaterialResolutionResponse> resolve(
            @RequestBody RfqMaterialResolutionRequest request
    ) {
        if (request == null
                || request.materialText() == null
                || request.materialText().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(
                resolutionService.resolve(request.materialText())
        );
    }
}
