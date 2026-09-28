package offeria.material_service.service.impl;

import offeria.material_service.domain.entity.Material;
import offeria.material_service.domain.entity.MaterialAlias;
import offeria.material_service.domain.enums.AliasLanguage;
import offeria.material_service.domain.enums.AliasType;
import offeria.material_service.domain.enums.MaterialSource;
import offeria.material_service.domain.enums.MaterialStatus;
import offeria.material_service.exception.ResourceNotFoundException;
import offeria.material_service.repository.MaterialAliasRepository;
import offeria.material_service.repository.MaterialRepository;
import offeria.material_service.service.normalization.MaterialNameNormalizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaterialAliasServiceImplTest {

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private MaterialAliasRepository materialAliasRepository;

    @Mock
    private MaterialNameNormalizer materialNameNormalizer;

    @InjectMocks
    private MaterialAliasServiceImpl materialAliasService;

    @Test
    void createAlias_ShouldCreatePendingReviewAlias() {
        UUID materialId = UUID.randomUUID();

        Material material = Material.builder()
                .id(materialId)
                .canonicalEnglishName("Oil Filter")
                .build();

        when(materialRepository.findById(materialId))
                .thenReturn(Optional.of(material));

        when(materialNameNormalizer.normalize(
                "  ENGINE   OIL FILTER  ",
                AliasLanguage.ENGLISH
        )).thenReturn("engine oil filter");

        when(materialAliasRepository.save(any(MaterialAlias.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MaterialAlias result = materialAliasService.createAlias(
                materialId,
                "  ENGINE   OIL FILTER  ",
                AliasLanguage.ENGLISH,
                AliasType.SYNONYM,
                MaterialSource.MANUAL
        );

        assertEquals(materialId, result.getMaterial().getId());
        assertEquals("  ENGINE   OIL FILTER  ", result.getAlias());
        assertEquals("engine oil filter", result.getNormalizedAlias());
        assertEquals(AliasLanguage.ENGLISH, result.getLanguage());
        assertEquals(AliasType.SYNONYM, result.getAliasType());
        assertEquals(MaterialSource.MANUAL, result.getSource());
        assertEquals(MaterialStatus.PENDING_REVIEW, result.getStatus());
    }

    @Test
    void approveAlias_ShouldChangePendingAliasToApproved() {
        UUID aliasId = UUID.randomUUID();

        MaterialAlias alias = MaterialAlias.builder()
                .id(aliasId)
                .status(MaterialStatus.PENDING_REVIEW)
                .build();

        when(materialAliasRepository.findById(aliasId))
                .thenReturn(Optional.of(alias));

        when(materialAliasRepository.save(any(MaterialAlias.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MaterialAlias result = materialAliasService.approveAlias(aliasId);

        assertEquals(MaterialStatus.APPROVED, result.getStatus());
    }

    @Test
    void rejectAlias_ShouldChangePendingAliasToRejected() {
        UUID aliasId = UUID.randomUUID();

        MaterialAlias alias = MaterialAlias.builder()
                .id(aliasId)
                .status(MaterialStatus.PENDING_REVIEW)
                .build();

        when(materialAliasRepository.findById(aliasId))
                .thenReturn(Optional.of(alias));

        when(materialAliasRepository.save(any(MaterialAlias.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MaterialAlias result = materialAliasService.rejectAlias(aliasId);

        assertEquals(MaterialStatus.REJECTED, result.getStatus());
    }

    @Test
    void approveAlias_ShouldRejectTransitionFromApproved() {
        UUID aliasId = UUID.randomUUID();

        MaterialAlias alias = MaterialAlias.builder()
                .id(aliasId)
                .status(MaterialStatus.APPROVED)
                .build();

        when(materialAliasRepository.findById(aliasId))
                .thenReturn(Optional.of(alias));

        assertThrows(
                IllegalStateException.class,
                () -> materialAliasService.approveAlias(aliasId)
        );
    }

    @Test
    void rejectAlias_ShouldRejectTransitionFromRejected() {
        UUID aliasId = UUID.randomUUID();

        MaterialAlias alias = MaterialAlias.builder()
                .id(aliasId)
                .status(MaterialStatus.REJECTED)
                .build();

        when(materialAliasRepository.findById(aliasId))
                .thenReturn(Optional.of(alias));

        assertThrows(
                IllegalStateException.class,
                () -> materialAliasService.rejectAlias(aliasId)
        );
    }

    @Test
    void createAlias_ShouldThrowWhenMaterialDoesNotExist() {
        UUID materialId = UUID.randomUUID();

        when(materialRepository.findById(materialId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> materialAliasService.createAlias(
                        materialId,
                        "Oil Filter",
                        AliasLanguage.ENGLISH,
                        AliasType.SYNONYM,
                        MaterialSource.MANUAL
                )
        );
    }

    @Test
    void getAliasById_ShouldReturnAliasForReview() {
        UUID aliasId = UUID.randomUUID();

        MaterialAlias alias = MaterialAlias.builder()
                .id(aliasId)
                .alias("فلتر دهن")
                .language(AliasLanguage.ARABIC)
                .status(MaterialStatus.PENDING_REVIEW)
                .build();

        when(materialAliasRepository.findById(aliasId))
                .thenReturn(Optional.of(alias));

        MaterialAlias result = materialAliasService.getAliasById(aliasId);

        assertEquals(aliasId, result.getId());
        assertEquals("فلتر دهن", result.getAlias());
        assertEquals(MaterialStatus.PENDING_REVIEW, result.getStatus());
    }
}
