package offeria.material_service.service.importing;

import offeria.material_service.domain.entity.LegacyMaterialStaging;
import offeria.material_service.domain.enums.LegacyMaterialImportStatus;
import offeria.material_service.exception.ResourceNotFoundException;
import offeria.material_service.repository.LegacyMaterialStagingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LegacyMaterialReviewServiceTest {

    @Mock
    private LegacyMaterialStagingRepository stagingRepository;

    @InjectMocks
    private LegacyMaterialReviewService service;

    @Test
    void shouldApprovePendingRecord() {

        UUID id = UUID.randomUUID();
        LegacyMaterialStaging staging = pendingRecord(id);

        when(stagingRepository.findById(id))
                .thenReturn(Optional.of(staging));

        when(stagingRepository.save(staging))
                .thenReturn(staging);

        LegacyMaterialStaging result =
                service.approve(
                        id,
                        "Verified legacy material"
                );

        assertThat(result.getStatus())
                .isEqualTo(
                        LegacyMaterialImportStatus.APPROVED_FOR_IMPORT
                );

        assertThat(result.getReviewNotes())
                .isEqualTo("Verified legacy material");

        assertThat(result.getRawValue())
                .isEqualTo("Oil Filter");

        verify(stagingRepository).save(staging);
    }

    @Test
    void shouldRejectPendingRecord() {

        UUID id = UUID.randomUUID();
        LegacyMaterialStaging staging = pendingRecord(id);

        when(stagingRepository.findById(id))
                .thenReturn(Optional.of(staging));

        when(stagingRepository.save(staging))
                .thenReturn(staging);

        LegacyMaterialStaging result =
                service.reject(
                        id,
                        "Invalid legacy value"
                );

        assertThat(result.getStatus())
                .isEqualTo(
                        LegacyMaterialImportStatus.REJECTED
                );

        assertThat(result.getReviewNotes())
                .isEqualTo("Invalid legacy value");

        assertThat(result.getRawValue())
                .isEqualTo("Oil Filter");

        verify(stagingRepository).save(staging);
    }

    @Test
    void shouldRejectReviewOfApprovedRecord() {

        UUID id = UUID.randomUUID();
        LegacyMaterialStaging staging = pendingRecord(id);

        staging.setStatus(
                LegacyMaterialImportStatus.APPROVED_FOR_IMPORT
        );

        when(stagingRepository.findById(id))
                .thenReturn(Optional.of(staging));

        assertThatThrownBy(
                () -> service.approve(id, "Again")
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "Only PENDING_REVIEW staging records can be reviewed"
                );

        verify(stagingRepository, never())
                .save(any());
    }

    @Test
    void shouldRejectReviewOfImportedRecord() {

        UUID id = UUID.randomUUID();
        LegacyMaterialStaging staging = pendingRecord(id);

        staging.setStatus(
                LegacyMaterialImportStatus.IMPORTED
        );

        when(stagingRepository.findById(id))
                .thenReturn(Optional.of(staging));

        assertThatThrownBy(
                () -> service.reject(id, "Reject imported")
        )
                .isInstanceOf(IllegalStateException.class);

        verify(stagingRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowWhenStagingRecordDoesNotExist() {

        UUID id = UUID.randomUUID();

        when(stagingRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> service.approve(id, "Review")
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(
                        "Legacy staging record not found: " + id
                );

        verify(stagingRepository, never())
                .save(any());
    }

    private LegacyMaterialStaging pendingRecord(UUID id) {

        return LegacyMaterialStaging.builder()
                .id(id)
                .rawValue("Oil Filter")
                .candidateEnglishName("Oil Filter")
                .normalizedEnglishName("oil filter")
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
