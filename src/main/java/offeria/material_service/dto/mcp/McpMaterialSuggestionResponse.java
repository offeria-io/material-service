package offeria.material_service.dto.mcp;

public record McpMaterialSuggestionResponse(
        String inputText,
        String canonicalEnglishName,
        String preferredIraqiName,
        String standardArabicName,
        String unit,
        String category,
        String specification,
        boolean approved
) {
}
