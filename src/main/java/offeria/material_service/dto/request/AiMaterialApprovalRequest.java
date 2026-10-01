package offeria.material_service.dto.request;

public record AiMaterialApprovalRequest(
        String canonicalEnglishName,
        String preferredIraqiName,
        String standardArabicName,
        String unit,
        String category,
        String specification
) {
}
