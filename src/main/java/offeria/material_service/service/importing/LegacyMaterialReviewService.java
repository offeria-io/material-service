package offeria.material_service.service.importing;

import lombok.RequiredArgsConstructor;
import offeria.material_service.domain.entity.LegacyMaterialStaging;
import offeria.material_service.domain.enums.LegacyMaterialImportStatus;
import offeria.material_service.exception.ResourceNotFoundException;
import offeria.material_service.repository.LegacyMaterialStagingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LegacyMaterialReviewService {

    private final LegacyMaterialStagingRepository stagingRepository;

    @Transactional
    public LegacyMaterialStaging approve(
            UUID stagingId,
            String reviewNotes
    ) {
        LegacyMaterialStaging staging =
                getPendingRecord(stagingId);

        staging.setStatus(
                LegacyMaterialImportStatus.APPROVED_FOR_IMPORT
        );
        staging.setReviewNotes(reviewNotes);

        return stagingRepository.save(staging);
    }

    @Transactional
    public LegacyMaterialStaging reject(
            UUID stagingId,
            String reviewNotes
    ) {
        LegacyMaterialStaging staging =
                getPendingRecord(stagingId);

        staging.setStatus(
                LegacyMaterialImportStatus.REJECTED
        );
        staging.setReviewNotes(reviewNotes);

        return stagingRepository.save(staging);
    }

    private LegacyMaterialStaging getPendingRecord(
            UUID stagingId
    ) {
        LegacyMaterialStaging staging =
                stagingRepository.findById(stagingId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Legacy staging record not found: "
                                                + stagingId
                                )
                        );

        if (staging.getStatus()
                != LegacyMaterialImportStatus.PENDING_REVIEW) {

            throw new IllegalStateException(
                    "Only PENDING_REVIEW staging records can be reviewed"
            );
        }

        return staging;
    }
}
