package offeria.material_service.dto.response;

public record AiMaterialSuggestionResponse(
        String inputText,
        String canonicalEnglishName,
        String preferredIraqiName,
        String standardArabicName,
        String unit,
        String category,
        String specification,
        boolean approved
) {

    public static AiMaterialSuggestionResponse unapproved(
            String inputText,
            String canonicalEnglishName,
            String preferredIraqiName,
            String standardArabicName,
            String unit,
            String category,
            String specification
    ) {
        return new AiMaterialSuggestionResponse(
                inputText,
                canonicalEnglishName,
                preferredIraqiName,
                standardArabicName,
                unit,
                category,
                specification,
                false
        );
    }
}
