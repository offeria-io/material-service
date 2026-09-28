package offeria.material_service.service.impl;

import lombok.RequiredArgsConstructor;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MaterialAliasServiceImpl {

    private final MaterialRepository materialRepository;
    private final MaterialAliasRepository materialAliasRepository;
    private final MaterialNameNormalizer materialNameNormalizer;

    @Transactional
    public MaterialAlias createAlias(
            UUID materialId,
            String alias,
            AliasLanguage language,
            AliasType aliasType,
            MaterialSource source
    ) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Material not found: " + materialId
                        )
                );

        String normalizedAlias =
                materialNameNormalizer.normalize(alias, language);

        MaterialAlias materialAlias = MaterialAlias.builder()
                .material(material)
                .alias(alias)
                .normalizedAlias(normalizedAlias)
                .language(language)
                .aliasType(aliasType)
                .source(source)
                .status(MaterialStatus.PENDING_REVIEW)
                .preferred(false)
                .build();

        return materialAliasRepository.save(materialAlias);
    }

    @Transactional
    public MaterialAlias approveAlias(UUID aliasId) {
        MaterialAlias alias = getPendingAlias(aliasId);
        alias.setStatus(MaterialStatus.APPROVED);
        return materialAliasRepository.save(alias);
    }

    @Transactional
    public MaterialAlias rejectAlias(UUID aliasId) {
        MaterialAlias alias = getPendingAlias(aliasId);
        alias.setStatus(MaterialStatus.REJECTED);
        return materialAliasRepository.save(alias);
    }

    private MaterialAlias getPendingAlias(UUID aliasId) {
        MaterialAlias alias = materialAliasRepository.findById(aliasId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Material alias not found: " + aliasId
                        )
                );

        if (alias.getStatus() != MaterialStatus.PENDING_REVIEW) {
            throw new IllegalStateException(
                    "Only PENDING_REVIEW aliases can be approved or rejected"
            );
        }

        return alias;
    }

    @Transactional(readOnly = true)
    public MaterialAlias getAliasById(UUID aliasId) {
        return materialAliasRepository.findById(aliasId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Material alias not found: " + aliasId
                        )
                );
    }
}
