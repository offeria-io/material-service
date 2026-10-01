package offeria.material_service.dto.mcp;

import java.util.List;
import java.util.UUID;

public record McpMaterialToolResponse(
        String tool,
        String query,
        boolean resolved,
        UUID materialId,
        String canonicalEnglishName,
        String preferredIraqiName,
        String standardArabicName,
        String unit,
        String category,
        String specification,
        String matchType,
        Double similarity,
        boolean approved,
        List<McpMaterialToolResponse> matches
) {

    public static McpMaterialToolResponse unresolved(
            String tool,
            String query
    ) {
        return new McpMaterialToolResponse(
                tool,
                query,
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                List.of()
        );
    }
}
