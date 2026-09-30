package offeria.material_service.service.importing;

import offeria.material_service.domain.entity.LegacyMaterialStaging;
import offeria.material_service.domain.enums.LegacyMaterialImportStatus;
import offeria.material_service.repository.LegacyMaterialStagingRepository;
import offeria.material_service.service.normalization.MaterialNameNormalizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LegacyCandidatePreparationServiceTest {

    @Mock
    private LegacyMaterialStagingRepository stagingRepository;

    @Mock
    private MaterialNameNormalizer materialNameNormalizer;

    @InjectMocks
    private LegacyCandidatePreparationService service;

    @Test
    void shouldPrepareEnglishCandidateFromPendingRecord() {

        LegacyMaterialStaging record =
                pendingRecord("  Oil   Filter  ");

        when(stagingRepository.findByStatus(
                LegacyMaterialImportStatus.PENDING_REVIEW
        )).thenReturn(List.of(record));

        when(materialNameNormalizer.normalizeEnglish(
                "  Oil   Filter  "
        )).thenReturn("oil filter");

        int prepared = service.preparePendingCandidates();

        assertThat(prepared).isEqualTo(1);

        assertThat(record.getRawValue())
                .isEqualTo("  Oil   Filter  ");

        assertThat(record.getCandidateEnglishName())
                .isEqualTo("  Oil   Filter  ");

        assertThat(record.getNormalizedEnglishName())
                .isEqualTo("oil filter");

        assertThat(record.getCandidateIraqiName())
                .isNull();

        assertThat(record.getStatus())
                .isEqualTo(
                        LegacyMaterialImportStatus.PENDING_REVIEW
                );

        verify(stagingRepository)
                .saveAll(List.of(record));
    }

    @Test
    void shouldPrepareIraqiCandidateFromPendingRecord() {

        LegacyMaterialStaging record =
                pendingRecord("  فلتر   دهن  ");

        when(stagingRepository.findByStatus(
                LegacyMaterialImportStatus.PENDING_REVIEW
        )).thenReturn(List.of(record));

        when(materialNameNormalizer.normalizeArabic(
                "  فلتر   دهن  "
        )).thenReturn("فلتر دهن");

        int prepared = service.preparePendingCandidates();

        assertThat(prepared).isEqualTo(1);

        assertThat(record.getRawValue())
                .isEqualTo("  فلتر   دهن  ");

        assertThat(record.getCandidateIraqiName())
                .isEqualTo("  فلتر   دهن  ");

        assertThat(record.getNormalizedIraqiName())
                .isEqualTo("فلتر دهن");

        assertThat(record.getCandidateEnglishName())
                .isNull();

        assertThat(record.getStatus())
                .isEqualTo(
                        LegacyMaterialImportStatus.PENDING_REVIEW
                );
    }

    @Test
    void shouldPreserveMixedLegacyRawValue() {

        String rawValue = "8-راس روط سعر 200$";

        LegacyMaterialStaging record =
                pendingRecord(rawValue);

        when(stagingRepository.findByStatus(
                LegacyMaterialImportStatus.PENDING_REVIEW
        )).thenReturn(List.of(record));

        when(materialNameNormalizer.normalizeArabic(rawValue))
                .thenReturn(rawValue);

        service.preparePendingCandidates();

        assertThat(record.getRawValue())
                .isEqualTo(rawValue);

        assertThat(record.getCandidateIraqiName())
                .isEqualTo(rawValue);

        assertThat(record.getStatus())
                .isEqualTo(
                        LegacyMaterialImportStatus.PENDING_REVIEW
                );
    }

    @Test
    void shouldNotProcessNonPendingRecords() {

        when(stagingRepository.findByStatus(
                LegacyMaterialImportStatus.PENDING_REVIEW
        )).thenReturn(List.of());

        int prepared = service.preparePendingCandidates();

        assertThat(prepared).isZero();

        verifyNoInteractions(materialNameNormalizer);

        verify(stagingRepository)
                .saveAll(List.of());
    }

    @Test
    void shouldSkipBlankRawValues() {

        LegacyMaterialStaging record =
                pendingRecord("   ");

        when(stagingRepository.findByStatus(
                LegacyMaterialImportStatus.PENDING_REVIEW
        )).thenReturn(List.of(record));

        int prepared = service.preparePendingCandidates();

        assertThat(prepared).isZero();

        assertThat(record.getCandidateEnglishName())
                .isNull();

        assertThat(record.getCandidateIraqiName())
                .isNull();

        verifyNoInteractions(materialNameNormalizer);
    }

    private LegacyMaterialStaging pendingRecord(
            String rawValue
    ) {

        return LegacyMaterialStaging.builder()
                .rawValue(rawValue)
                .sourceWorkbook("legacy.xlsx")
                .sourceSheet("Materials")
                .sourceRow(1)
                .sourceColumn("A")
                .status(
                        LegacyMaterialImportStatus.PENDING_REVIEW
                )
                .build();
    }
}
