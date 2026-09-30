package offeria.material_service.service.importing;

import lombok.RequiredArgsConstructor;
import offeria.material_service.domain.entity.LegacyMaterialStaging;
import offeria.material_service.domain.enums.LegacyMaterialImportStatus;
import offeria.material_service.repository.LegacyMaterialStagingRepository;
import offeria.material_service.service.normalization.MaterialNameNormalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class LegacyCandidatePreparationService {

    private static final Pattern ARABIC_PATTERN =
            Pattern.compile(".*[\\p{InArabic}].*");

    private final LegacyMaterialStagingRepository stagingRepository;
    private final MaterialNameNormalizer materialNameNormalizer;

    @Transactional
    public int preparePendingCandidates() {

        List<LegacyMaterialStaging> pendingRecords =
                stagingRepository.findByStatus(
                        LegacyMaterialImportStatus.PENDING_REVIEW
                );

        int preparedCount = 0;

        for (LegacyMaterialStaging record : pendingRecords) {

            if (prepareCandidate(record)) {
                preparedCount++;
            }
        }

        stagingRepository.saveAll(pendingRecords);

        return preparedCount;
    }

    private boolean prepareCandidate(
            LegacyMaterialStaging record
    ) {

        String rawValue = record.getRawValue();

        if (rawValue == null || rawValue.isBlank()) {
            return false;
        }

        if (containsArabic(rawValue)) {

            record.setCandidateIraqiName(rawValue);

            record.setNormalizedIraqiName(
                    materialNameNormalizer.normalizeArabic(rawValue)
            );

        } else {

            record.setCandidateEnglishName(rawValue);

            record.setNormalizedEnglishName(
                    materialNameNormalizer.normalizeEnglish(rawValue)
            );
        }

        return true;
    }

    private boolean containsArabic(String value) {
        return ARABIC_PATTERN.matcher(value).matches();
    }
}
