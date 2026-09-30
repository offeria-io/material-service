package offeria.material_service.controller;

import lombok.RequiredArgsConstructor;
import offeria.material_service.domain.entity.LegacyMaterialStaging;
import offeria.material_service.domain.enums.LegacyMaterialImportStatus;
import offeria.material_service.dto.request.LegacyMaterialReviewRequest;
import offeria.material_service.dto.request.LegacyMaterialBatchReviewRequest;
import offeria.material_service.dto.request.LegacyMaterialBatchPromotionRequest;
import offeria.material_service.dto.response.LegacyMaterialStagingResponse;
import offeria.material_service.dto.response.LegacyMaterialPromotionResponse;
import offeria.material_service.dto.response.LegacyMaterialBatchItemResponse;
import offeria.material_service.exception.ResourceNotFoundException;
import offeria.material_service.repository.LegacyMaterialStagingRepository;
import offeria.material_service.service.importing.LegacyMaterialReviewService;
import offeria.material_service.service.importing.LegacyMaterialPromotionService;
import offeria.material_service.service.importing.LegacyMaterialBatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/legacy-material-staging")
@RequiredArgsConstructor
public class LegacyMaterialStagingController {

    private final LegacyMaterialStagingRepository stagingRepository;
    private final LegacyMaterialReviewService reviewService;
    private final LegacyMaterialPromotionService promotionService;
    private final LegacyMaterialBatchService batchService;

    @GetMapping
    public ResponseEntity<List<LegacyMaterialStagingResponse>> getByStatus(
            @RequestParam(defaultValue = "PENDING_REVIEW")
            LegacyMaterialImportStatus status
    ) {
        List<LegacyMaterialStagingResponse> response =
                stagingRepository.findByStatus(status)
                        .stream()
                        .map(LegacyMaterialStagingResponse::from)
                        .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LegacyMaterialStagingResponse> getById(
            @PathVariable UUID id
    ) {
        LegacyMaterialStaging staging =
                stagingRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Legacy staging record not found: " + id
                                )
                        );

        return ResponseEntity.ok(
                LegacyMaterialStagingResponse.from(staging)
        );
    }


    @PostMapping("/batch/approve")
    public ResponseEntity<List<LegacyMaterialBatchItemResponse>> batchApprove(
            @RequestBody LegacyMaterialBatchReviewRequest request
    ) {
        return ResponseEntity.ok(
                batchService.approve(
                        request.stagingIds(),
                        request.reviewNotes()
                )
        );
    }

    @PostMapping("/batch/reject")
    public ResponseEntity<List<LegacyMaterialBatchItemResponse>> batchReject(
            @RequestBody LegacyMaterialBatchReviewRequest request
    ) {
        return ResponseEntity.ok(
                batchService.reject(
                        request.stagingIds(),
                        request.reviewNotes()
                )
        );
    }

    @PostMapping("/batch/promote")
    public ResponseEntity<List<LegacyMaterialBatchItemResponse>> batchPromote(
            @RequestBody LegacyMaterialBatchPromotionRequest request
    ) {
        return ResponseEntity.ok(
                batchService.promote(request.stagingIds())
        );
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<LegacyMaterialStagingResponse> approve(
            @PathVariable UUID id,
            @RequestBody(required = false)
            LegacyMaterialReviewRequest request
    ) {
        String notes =
                request == null ? null : request.reviewNotes();

        return ResponseEntity.ok(
                LegacyMaterialStagingResponse.from(
                        reviewService.approve(id, notes)
                )
        );
    }


    @PostMapping("/{id}/promote")
    public ResponseEntity<LegacyMaterialPromotionResponse> promote(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                LegacyMaterialPromotionResponse.from(
                        promotionService.promote(id)
                )
        );
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<LegacyMaterialStagingResponse> reject(
            @PathVariable UUID id,
            @RequestBody(required = false)
            LegacyMaterialReviewRequest request
    ) {
        String notes =
                request == null ? null : request.reviewNotes();

        return ResponseEntity.ok(
                LegacyMaterialStagingResponse.from(
                        reviewService.reject(id, notes)
                )
        );
    }
}
