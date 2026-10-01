package offeria.material_service.controller;

import lombok.RequiredArgsConstructor;
import offeria.material_service.dto.request.AiMaterialApprovalRequest;
import offeria.material_service.dto.response.AiMaterialApprovalResponse;
import offeria.material_service.service.ai.AiMaterialApprovalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/materials/ai")
@RequiredArgsConstructor
public class AiMaterialApprovalController {

    private final AiMaterialApprovalService approvalService;

    @PostMapping("/approve")
    public ResponseEntity<AiMaterialApprovalResponse> approve(
            @RequestBody AiMaterialApprovalRequest request
    ) {
        AiMaterialApprovalResponse response = approvalService.approve(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
