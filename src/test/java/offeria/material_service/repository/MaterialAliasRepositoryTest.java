package offeria.material_service.repository;

import offeria.material_service.domain.entity.Material;
import offeria.material_service.domain.entity.MaterialAlias;
import offeria.material_service.domain.enums.AliasLanguage;
import offeria.material_service.domain.enums.AliasType;
import offeria.material_service.domain.enums.MaterialSource;
import offeria.material_service.domain.enums.MaterialStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class MaterialAliasRepositoryTest {

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private MaterialAliasRepository materialAliasRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldNotPersistAliasWithoutMaterial() {
        MaterialAlias alias = MaterialAlias.builder()
                .alias("Oil Filter")
                .normalizedAlias("oil filter")
                .language(AliasLanguage.ENGLISH)
                .aliasType(AliasType.SYNONYM)
                .source(MaterialSource.MANUAL)
                .status(MaterialStatus.PENDING_REVIEW)
                .preferred(false)
                .build();

        assertThatThrownBy(() ->
                materialAliasRepository.saveAndFlush(alias)
        ).isInstanceOf(Exception.class);
    }

    @Test
    void shouldPersistMultipleAliasesForOneMaterial() {
        Material material = Material.builder()
                .canonicalEnglishName("Oil Filter")
                .preferredIraqiName("فلتر دهن")
                .unit("PCS")
                .status(MaterialStatus.APPROVED)
                .source(MaterialSource.MANUAL)
                .build();

        Material savedMaterial = materialRepository.saveAndFlush(material);

        MaterialAlias englishAlias = MaterialAlias.builder()
                .material(savedMaterial)
                .alias("Engine Oil Filter")
                .normalizedAlias("engine oil filter")
                .language(AliasLanguage.ENGLISH)
                .aliasType(AliasType.SYNONYM)
                .source(MaterialSource.MANUAL)
                .status(MaterialStatus.APPROVED)
                .preferred(false)
                .build();

        MaterialAlias iraqiAlias = MaterialAlias.builder()
                .material(savedMaterial)
                .alias("فلتر دهن")
                .normalizedAlias(null)
                .language(AliasLanguage.ARABIC)
                .aliasType(AliasType.MARKET_NAME)
                .source(MaterialSource.LEGACY_EXCEL)
                .status(MaterialStatus.PENDING_REVIEW)
                .preferred(true)
                .build();

        materialAliasRepository.saveAllAndFlush(
                List.of(englishAlias, iraqiAlias)
        );

        entityManager.clear();

        List<MaterialAlias> aliases = materialAliasRepository.findAll();

        assertThat(aliases).hasSize(2);

        assertThat(aliases)
                .extracting(MaterialAlias::getAlias)
                .containsExactlyInAnyOrder(
                        "Engine Oil Filter",
                        "فلتر دهن"
                );

        assertThat(aliases)
                .allSatisfy(alias ->
                        assertThat(alias.getMaterial().getId())
                                .isEqualTo(savedMaterial.getId())
                );

        MaterialAlias persistedIraqiAlias = aliases.stream()
                .filter(alias -> alias.getAlias().equals("فلتر دهن"))
                .findFirst()
                .orElseThrow();

        assertThat(persistedIraqiAlias.getNormalizedAlias()).isNull();
        assertThat(persistedIraqiAlias.getLanguage())
                .isEqualTo(AliasLanguage.ARABIC);
        assertThat(persistedIraqiAlias.getAliasType())
                .isEqualTo(AliasType.MARKET_NAME);
        assertThat(persistedIraqiAlias.getSource())
                .isEqualTo(MaterialSource.LEGACY_EXCEL);
        assertThat(persistedIraqiAlias.getStatus())
                .isEqualTo(MaterialStatus.PENDING_REVIEW);
        assertThat(persistedIraqiAlias.isPreferred()).isTrue();
        assertThat(persistedIraqiAlias.getCreatedAt()).isNotNull();
        assertThat(persistedIraqiAlias.getUpdatedAt()).isNotNull();
    }
}
