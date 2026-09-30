package offeria.material_service.repository;

import offeria.material_service.domain.entity.LegacyMaterialStaging;
import offeria.material_service.domain.enums.LegacyMaterialImportStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class LegacyMaterialStagingRepositoryTest {

    @Autowired
    private LegacyMaterialStagingRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldPersistRawLegacyValueWithoutModification() {
        String rawValue = "8-راس روط سعر 200$";

        LegacyMaterialStaging stagingRecord =
                LegacyMaterialStaging.builder()
                        .rawValue(rawValue)
                        .sourceWorkbook("legacy-spare-parts.xlsx")
                        .sourceSheet("Sheet1")
                        .sourceRow(8)
                        .sourceColumn("B")
                        .status(LegacyMaterialImportStatus.PENDING_REVIEW)
                        .build();

        LegacyMaterialStaging saved =
                repository.saveAndFlush(stagingRecord);

        entityManager.clear();

        LegacyMaterialStaging persisted =
                repository.findById(saved.getId())
                        .orElseThrow();

        assertThat(persisted.getRawValue())
                .isEqualTo(rawValue);

        assertThat(persisted.getStatus())
                .isEqualTo(LegacyMaterialImportStatus.PENDING_REVIEW);

        assertThat(persisted.getSourceWorkbook())
                .isEqualTo("legacy-spare-parts.xlsx");

        assertThat(persisted.getSourceSheet())
                .isEqualTo("Sheet1");

        assertThat(persisted.getSourceRow())
                .isEqualTo(8);

        assertThat(persisted.getSourceColumn())
                .isEqualTo("B");

        assertThat(persisted.getCreatedAt()).isNotNull();
        assertThat(persisted.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldStoreCandidateValuesSeparatelyFromRawValue() {
        LegacyMaterialStaging stagingRecord =
                LegacyMaterialStaging.builder()
                        .rawValue("8-راس روط سعر 200$")
                        .candidateEnglishName("Tie Rod End")
                        .candidateIraqiName("راس روط")
                        .candidateStandardArabicName("طرف قضيب التوجيه")
                        .normalizedEnglishName("tie rod end")
                        .normalizedIraqiName("راس روط")
                        .sourceWorkbook("legacy-spare-parts.xlsx")
                        .sourceSheet("Sheet1")
                        .sourceRow(8)
                        .sourceColumn("B")
                        .status(LegacyMaterialImportStatus.PENDING_REVIEW)
                        .build();

        LegacyMaterialStaging saved =
                repository.saveAndFlush(stagingRecord);

        entityManager.clear();

        LegacyMaterialStaging persisted =
                repository.findById(saved.getId())
                        .orElseThrow();

        assertThat(persisted.getRawValue())
                .isEqualTo("8-راس روط سعر 200$");

        assertThat(persisted.getCandidateEnglishName())
                .isEqualTo("Tie Rod End");

        assertThat(persisted.getCandidateIraqiName())
                .isEqualTo("راس روط");

        assertThat(persisted.getCandidateStandardArabicName())
                .isEqualTo("طرف قضيب التوجيه");

        assertThat(persisted.getNormalizedEnglishName())
                .isEqualTo("tie rod end");

        assertThat(persisted.getNormalizedIraqiName())
                .isEqualTo("راس روط");
    }

    @Test
    void shouldFindRecordsByReviewStatus() {
        repository.saveAndFlush(
                createRecord(
                        "Oil Filter",
                        1,
                        LegacyMaterialImportStatus.PENDING_REVIEW
                )
        );

        repository.saveAndFlush(
                createRecord(
                        "Fuel Filter",
                        2,
                        LegacyMaterialImportStatus.APPROVED_FOR_IMPORT
                )
        );

        entityManager.clear();

        List<LegacyMaterialStaging> pending =
                repository.findByStatus(
                        LegacyMaterialImportStatus.PENDING_REVIEW
                );

        assertThat(pending).hasSize(1);
        assertThat(pending.getFirst().getRawValue())
                .isEqualTo("Oil Filter");
    }

    @Test
    void shouldFindRecordsByWorkbookAndSheet() {
        repository.saveAndFlush(
                createRecord(
                        "Oil Filter",
                        1,
                        LegacyMaterialImportStatus.PENDING_REVIEW
                )
        );

        LegacyMaterialStaging anotherSheet =
                LegacyMaterialStaging.builder()
                        .rawValue("Fuel Filter")
                        .sourceWorkbook("legacy-spare-parts.xlsx")
                        .sourceSheet("Sheet2")
                        .sourceRow(2)
                        .sourceColumn("B")
                        .status(LegacyMaterialImportStatus.PENDING_REVIEW)
                        .build();

        repository.saveAndFlush(anotherSheet);

        entityManager.clear();

        List<LegacyMaterialStaging> records =
                repository.findBySourceWorkbookAndSourceSheet(
                        "legacy-spare-parts.xlsx",
                        "Sheet1"
                );

        assertThat(records).hasSize(1);
        assertThat(records.getFirst().getRawValue())
                .isEqualTo("Oil Filter");
    }

    private LegacyMaterialStaging createRecord(
            String rawValue,
            int row,
            LegacyMaterialImportStatus status
    ) {
        return LegacyMaterialStaging.builder()
                .rawValue(rawValue)
                .sourceWorkbook("legacy-spare-parts.xlsx")
                .sourceSheet("Sheet1")
                .sourceRow(row)
                .sourceColumn("B")
                .status(status)
                .build();
    }
}