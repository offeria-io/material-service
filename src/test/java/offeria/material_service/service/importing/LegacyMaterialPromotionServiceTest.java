package offeria.material_service.service.importing;

import offeria.material_service.domain.entity.LegacyMaterialStaging;
import offeria.material_service.domain.entity.Material;
import offeria.material_service.domain.entity.MaterialAlias;
import offeria.material_service.domain.enums.*;
import offeria.material_service.repository.LegacyMaterialStagingRepository;
import offeria.material_service.repository.MaterialAliasRepository;
import offeria.material_service.repository.MaterialRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LegacyMaterialPromotionServiceTest {

    @Mock
    private LegacyMaterialStagingRepository stagingRepository;

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private MaterialAliasRepository materialAliasRepository;

    @InjectMocks
    private LegacyMaterialPromotionService service;

    @Test
    void shouldPromoteApprovedStagingRecordIntoNewMaterial() {

        UUID stagingId = UUID.randomUUID();
        UUID materialId = UUID.randomUUID();

        LegacyMaterialStaging staging =
                approvedEnglishStaging(stagingId);

        when(stagingRepository.findById(stagingId))
                .thenReturn(Optional.of(staging));

        when(materialRepository
                .findByNormalizedEnglishName("oil filter"))
                .thenReturn(Optional.empty());

        when(materialRepository.save(any(Material.class)))
                .thenAnswer(invocation -> {
                    Material material = invocation.getArgument(0);
                    material.setId(materialId);
                    return material;
                });

        when(materialAliasRepository
                .findByNormalizedAliasAndStatus(
                        "oil filter",
                        MaterialStatus.APPROVED
                ))
                .thenReturn(Optional.empty());

        Material result = service.promote(stagingId);

        assertThat(result.getId()).isEqualTo(materialId);
        assertThat(result.getCanonicalEnglishName())
                .isEqualTo("Oil Filter");

        assertThat(result.getSource())
                .isEqualTo(MaterialSource.LEGACY_EXCEL);

        assertThat(result.getStatus())
                .isEqualTo(MaterialStatus.APPROVED);

        assertThat(result.getUnit())
                .isEqualTo("UNKNOWN");

        assertThat(staging.getImportedMaterialId())
                .isEqualTo(materialId);

        assertThat(staging.getStatus())
                .isEqualTo(
                        LegacyMaterialImportStatus.IMPORTED
                );

        assertThat(staging.getRawValue())
                .isEqualTo("Oil Filter");

        verify(stagingRepository).save(staging);
        verify(materialAliasRepository)
                .save(any(MaterialAlias.class));
    }

    @Test
    void shouldReuseExistingMaterial() {

        UUID stagingId = UUID.randomUUID();
        UUID materialId = UUID.randomUUID();

        LegacyMaterialStaging staging =
                approvedEnglishStaging(stagingId);

        Material existing =
                Material.builder()
                        .id(materialId)
                        .canonicalEnglishName("Oil Filter")
                        .normalizedEnglishName("oil filter")
                        .unit("PCS")
                        .status(MaterialStatus.APPROVED)
                        .source(MaterialSource.MANUAL)
                        .build();

        when(stagingRepository.findById(stagingId))
                .thenReturn(Optional.of(staging));

        when(materialRepository
                .findByNormalizedEnglishName("oil filter"))
                .thenReturn(Optional.of(existing));

        when(materialAliasRepository
                .findByNormalizedAliasAndStatus(
                        "oil filter",
                        MaterialStatus.APPROVED
                ))
                .thenReturn(Optional.empty());

        Material result = service.promote(stagingId);

        assertThat(result).isSameAs(existing);

        assertThat(staging.getImportedMaterialId())
                .isEqualTo(materialId);

        assertThat(staging.getStatus())
                .isEqualTo(
                        LegacyMaterialImportStatus.IMPORTED
                );

        verify(materialRepository, never())
                .save(any(Material.class));
    }

    @Test
    void shouldRejectPromotionWhenRecordIsNotApproved() {

        UUID stagingId = UUID.randomUUID();

        LegacyMaterialStaging staging =
                approvedEnglishStaging(stagingId);

        staging.setStatus(
                LegacyMaterialImportStatus.PENDING_REVIEW
        );

        when(stagingRepository.findById(stagingId))
                .thenReturn(Optional.of(staging));

        assertThatThrownBy(
                () -> service.promote(stagingId)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "Only APPROVED_FOR_IMPORT staging records can be promoted"
                );

        verifyNoInteractions(materialRepository);
        verifyNoInteractions(materialAliasRepository);
        verify(stagingRepository, never())
                .save(any());
    }

    @Test
    void shouldRequireEnglishCandidateForNewMaterial() {

        UUID stagingId = UUID.randomUUID();

        LegacyMaterialStaging staging =
                LegacyMaterialStaging.builder()
                        .id(stagingId)
                        .rawValue("فلتر دهن")
                        .candidateIraqiName("فلتر دهن")
                        .normalizedIraqiName("فلتر دهن")
                        .sourceWorkbook("legacy.xlsx")
                        .sourceSheet("Materials")
                        .status(
                                LegacyMaterialImportStatus.APPROVED_FOR_IMPORT
                        )
                        .build();

        when(stagingRepository.findById(stagingId))
                .thenReturn(Optional.of(staging));

        when(materialRepository
                .findByNormalizedIraqiName("فلتر دهن"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> service.promote(stagingId)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "Approved staging record requires an English candidate before creating a new material"
                );

        verify(materialRepository, never())
                .save(any(Material.class));

        assertThat(staging.getStatus())
                .isEqualTo(
                        LegacyMaterialImportStatus.APPROVED_FOR_IMPORT
                );
    }

    @Test
    void shouldNotCreateDuplicateApprovedAlias() {

        UUID stagingId = UUID.randomUUID();
        UUID materialId = UUID.randomUUID();

        LegacyMaterialStaging staging =
                approvedEnglishStaging(stagingId);

        Material existing =
                Material.builder()
                        .id(materialId)
                        .canonicalEnglishName("Oil Filter")
                        .normalizedEnglishName("oil filter")
                        .unit("PCS")
                        .status(MaterialStatus.APPROVED)
                        .source(MaterialSource.MANUAL)
                        .build();

        MaterialAlias existingAlias =
                MaterialAlias.builder()
                        .id(UUID.randomUUID())
                        .material(existing)
                        .alias("Oil Filter")
                        .normalizedAlias("oil filter")
                        .language(AliasLanguage.ENGLISH)
                        .aliasType(AliasType.LEGACY_NAME)
                        .source(MaterialSource.LEGACY_EXCEL)
                        .status(MaterialStatus.APPROVED)
                        .build();

        when(stagingRepository.findById(stagingId))
                .thenReturn(Optional.of(staging));

        when(materialRepository
                .findByNormalizedEnglishName("oil filter"))
                .thenReturn(Optional.of(existing));

        when(materialAliasRepository
                .findByNormalizedAliasAndStatus(
                        "oil filter",
                        MaterialStatus.APPROVED
                ))
                .thenReturn(Optional.of(existingAlias));

        service.promote(stagingId);

        verify(materialAliasRepository, never())
                .save(any(MaterialAlias.class));

        assertThat(staging.getStatus())
                .isEqualTo(
                        LegacyMaterialImportStatus.IMPORTED
                );
    }

    private LegacyMaterialStaging approvedEnglishStaging(
            UUID id
    ) {

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
                        LegacyMaterialImportStatus.APPROVED_FOR_IMPORT
                )
                .build();
    }
}
