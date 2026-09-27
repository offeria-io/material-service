package offeria.material_service.repository;

import offeria.material_service.domain.entity.Material;
import offeria.material_service.domain.enums.MaterialSource;
import offeria.material_service.domain.enums.MaterialStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class MaterialLookupRepositoryTest {

    @Autowired
    private MaterialRepository materialRepository;

    @Test
    void shouldFindMaterialByNormalizedEnglishName() {
        Material material = Material.builder()
                .canonicalEnglishName("Oil Filter")
                .preferredIraqiName("فلتر دهن")
                .normalizedEnglishName("oil filter")
                .normalizedIraqiName("فلتر دهن")
                .unit("PCS")
                .status(MaterialStatus.APPROVED)
                .source(MaterialSource.MANUAL)
                .build();

        materialRepository.saveAndFlush(material);

        Optional<Material> result =
                materialRepository.findByNormalizedEnglishName("oil filter");

        assertThat(result)
                .isPresent()
                .get()
                .extracting(Material::getCanonicalEnglishName)
                .isEqualTo("Oil Filter");
    }

    @Test
    void shouldFindMaterialByNormalizedIraqiName() {
        Material material = Material.builder()
                .canonicalEnglishName("Oil Filter")
                .preferredIraqiName("فلتر دهن")
                .normalizedEnglishName("oil filter")
                .normalizedIraqiName("فلتر دهن")
                .unit("PCS")
                .status(MaterialStatus.APPROVED)
                .source(MaterialSource.MANUAL)
                .build();

        materialRepository.saveAndFlush(material);

        Optional<Material> result =
                materialRepository.findByNormalizedIraqiName("فلتر دهن");

        assertThat(result)
                .isPresent()
                .get()
                .extracting(Material::getCanonicalEnglishName)
                .isEqualTo("Oil Filter");
    }

    @Test
    void shouldFailWhenMultipleMaterialsHaveSameNormalizedEnglishName() {
        Material firstMaterial = Material.builder()
                .canonicalEnglishName("Oil Filter")
                .normalizedEnglishName("oil filter")
                .unit("PCS")
                .status(MaterialStatus.APPROVED)
                .source(MaterialSource.MANUAL)
                .build();

        Material secondMaterial = Material.builder()
                .canonicalEnglishName("Engine Oil Filter")
                .normalizedEnglishName("oil filter")
                .unit("PCS")
                .status(MaterialStatus.APPROVED)
                .source(MaterialSource.IMPORT)
                .build();

        materialRepository.saveAndFlush(firstMaterial);
        materialRepository.saveAndFlush(secondMaterial);

        assertThatThrownBy(() ->
                materialRepository.findByNormalizedEnglishName("oil filter")
        ).isInstanceOf(IncorrectResultSizeDataAccessException.class);
    }
}
