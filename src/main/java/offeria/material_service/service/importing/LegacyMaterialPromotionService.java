package offeria.material_service.service.importing;

import lombok.RequiredArgsConstructor;
import offeria.material_service.domain.entity.LegacyMaterialStaging;
import offeria.material_service.domain.entity.Material;
import offeria.material_service.domain.entity.MaterialAlias;
import offeria.material_service.domain.enums.*;
import offeria.material_service.exception.ResourceNotFoundException;
import offeria.material_service.repository.LegacyMaterialStagingRepository;
import offeria.material_service.repository.MaterialAliasRepository;
import offeria.material_service.repository.MaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LegacyMaterialPromotionService {

    private final LegacyMaterialStagingRepository stagingRepository;
    private final MaterialRepository materialRepository;
    private final MaterialAliasRepository materialAliasRepository;

    @Transactional
    public Material promote(UUID stagingId) {

        LegacyMaterialStaging staging =
                stagingRepository.findById(stagingId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Legacy staging record not found: " + stagingId
                                )
                        );

        if (staging.getStatus()
                != LegacyMaterialImportStatus.APPROVED_FOR_IMPORT) {

            throw new IllegalStateException(
                    "Only APPROVED_FOR_IMPORT staging records can be promoted"
            );
        }

        Material material = findExistingMaterial(staging);

        if (material == null) {
            material = createMaterial(staging);
        }

        createLegacyAliasIfRequired(staging, material);

        staging.setImportedMaterialId(material.getId());
        staging.setStatus(LegacyMaterialImportStatus.IMPORTED);

        stagingRepository.save(staging);

        return material;
    }

    private Material findExistingMaterial(
            LegacyMaterialStaging staging
    ) {

        if (staging.getNormalizedEnglishName() != null) {

            Material material =
                    materialRepository
                            .findByNormalizedEnglishName(
                                    staging.getNormalizedEnglishName()
                            )
                            .orElse(null);

            if (material != null) {
                return material;
            }
        }

        if (staging.getNormalizedIraqiName() != null) {

            return materialRepository
                    .findByNormalizedIraqiName(
                            staging.getNormalizedIraqiName()
                    )
                    .orElse(null);
        }

        return null;
    }

    private Material createMaterial(
            LegacyMaterialStaging staging
    ) {

        String englishName =
                staging.getCandidateEnglishName();

        if (englishName == null || englishName.isBlank()) {
            throw new IllegalStateException(
                    "Approved staging record requires an English candidate before creating a new material"
            );
        }

        Material material =
                Material.builder()
                        .canonicalEnglishName(englishName)
                        .preferredIraqiName(
                                staging.getCandidateIraqiName()
                        )
                        .standardArabicName(
                                staging.getCandidateStandardArabicName()
                        )
                        .normalizedEnglishName(
                                staging.getNormalizedEnglishName()
                        )
                        .normalizedIraqiName(
                                staging.getNormalizedIraqiName()
                        )
                        .unit("UNKNOWN")
                        .status(MaterialStatus.APPROVED)
                        .source(MaterialSource.LEGACY_EXCEL)
                        .build();

        return materialRepository.save(material);
    }

    private void createLegacyAliasIfRequired(
            LegacyMaterialStaging staging,
            Material material
    ) {

        String rawValue = staging.getRawValue();

        if (rawValue == null || rawValue.isBlank()) {
            return;
        }

        String normalizedAlias;
        AliasLanguage language;

        if (staging.getCandidateIraqiName() != null) {

            normalizedAlias =
                    staging.getNormalizedIraqiName();

            language = AliasLanguage.ARABIC;

        } else {

            normalizedAlias =
                    staging.getNormalizedEnglishName();

            language = AliasLanguage.ENGLISH;
        }

        if (normalizedAlias == null) {
            return;
        }

        boolean alreadyExists =
                materialAliasRepository
                        .findByNormalizedAliasAndStatus(
                                normalizedAlias,
                                MaterialStatus.APPROVED
                        )
                        .isPresent();

        if (alreadyExists) {
            return;
        }

        MaterialAlias alias =
                MaterialAlias.builder()
                        .material(material)
                        .alias(rawValue)
                        .normalizedAlias(normalizedAlias)
                        .language(language)
                        .aliasType(AliasType.LEGACY_NAME)
                        .source(MaterialSource.LEGACY_EXCEL)
                        .status(MaterialStatus.APPROVED)
                        .preferred(false)
                        .build();

        materialAliasRepository.save(alias);
    }
}
