package offeria.material_service.service.normalization;

import offeria.material_service.domain.enums.AliasLanguage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MaterialNameNormalizerTest {

    private final MaterialNameNormalizer normalizer = new MaterialNameNormalizer();

    @Test
    void shouldReturnNullForBlankInput() {
        assertThat(normalizer.normalizeEnglish("   ")).isNull();
        assertThat(normalizer.normalizeArabic("   ")).isNull();
    }

    @Test
    void shouldRejectNullAliasLanguage() {
        assertThatThrownBy(() -> normalizer.normalize("Oil Filter", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Alias language must not be null");
    }

    @Test
    void shouldNormalizeEnglishName() {
        String result = normalizer.normalizeEnglish("  OIL   Filter  ");

        assertThat(result).isEqualTo("oil filter");
    }

    @Test
    void shouldPreserveMeaningfulEnglishPunctuation() {
        String result = normalizer.normalizeEnglish("  Shell   Rimula R4  15W-40  ");

        assertThat(result).isEqualTo("shell rimula r4 15w-40");
    }

    @Test
    void shouldNormalizeArabicWhitespace() {
        String result = normalizer.normalizeArabic("  فلتر   دهن  ");

        assertThat(result).isEqualTo("فلتر دهن");
    }

    @Test
    void shouldReturnNullForNullInput() {
        assertThat(normalizer.normalizeEnglish(null)).isNull();
        assertThat(normalizer.normalizeArabic(null)).isNull();
    }

    @Test
    void shouldNormalizeAliasAccordingToLanguage() {
        assertThat(normalizer.normalize("  OIL   Filter  ", AliasLanguage.ENGLISH))
                .isEqualTo("oil filter");

        assertThat(normalizer.normalize("  فلتر   دهن  ", AliasLanguage.ARABIC))
                .isEqualTo("فلتر دهن");
    }
}
