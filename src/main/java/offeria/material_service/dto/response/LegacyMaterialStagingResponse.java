package offeria.material_service.dto.response;

import offeria.material_service.domain.entity.LegacyMaterialStaging;
import offeria.material_service.domain.enums.LegacyMaterialImportStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record LegacyMaterialStagingResponse(
        UUID id,
        String rawValue,
        String candidateEnglishName,
        String candidateIraqiName,
        String candidateStandardArabicName,
        String normalizedEnglishName,
        String normalizedIraqiName,
        String sourceWorkbook,
        String sourceSheet,
        Integer sourceRow,
        String sourceColumn,
        LegacyMaterialImportStatus status,
        String reviewNotes,
        UUID importedMaterialId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static LegacyMaterialStagingResponse from(
            LegacyMaterialStaging staging
    ) {
        return new LegacyMaterialStagingResponse(
                staging.getId(),
                staging.getRawValue(),
                staging.getCandidateEnglishName(),
                staging.getCandidateIraqiName(),
                staging.getCandidateStandardArabicName(),
                staging.getNormalizedEnglishName(),
                staging.getNormalizedIraqiName(),
                staging.getSourceWorkbook(),
                staging.getSourceSheet(),
                staging.getSourceRow(),
                staging.getSourceColumn(),
                staging.getStatus(),
                staging.getReviewNotes(),
                staging.getImportedMaterialId(),
                staging.getCreatedAt(),
                staging.getUpdatedAt()
        );
    }
}
