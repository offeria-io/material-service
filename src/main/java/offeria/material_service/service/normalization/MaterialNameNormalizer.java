package offeria.material_service.service.normalization;

import offeria.material_service.domain.enums.AliasLanguage;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;

@Component
public class MaterialNameNormalizer {

    public String normalizeEnglish(String value) {
        String normalized = normalizeWhitespace(value);

        if (normalized == null) {
            return null;
        }

        return normalized.toLowerCase(Locale.ROOT);
    }

    public String normalizeArabic(String value) {
        return normalizeWhitespace(value);
    }

    public String normalize(String value, AliasLanguage language) {
        if (language == null) {
            throw new IllegalArgumentException("Alias language must not be null");
        }

        return switch (language) {
            case ENGLISH -> normalizeEnglish(value);
            case ARABIC -> normalizeArabic(value);
        };
    }

    private String normalizeWhitespace(String value) {
        if (value == null) {
            return null;
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC)
                .trim()
                .replaceAll("\\s+", " ");

        return normalized.isEmpty() ? null : normalized;
    }
}
