package offeria.material_service.dto.response;

import java.util.UUID;

public record LegacyMaterialBatchItemResponse(
        UUID stagingId,
        boolean success,
        String message,
        UUID materialId
) {

    public static LegacyMaterialBatchItemResponse success(
            UUID stagingId
    ) {
        return new LegacyMaterialBatchItemResponse(
                stagingId,
                true,
                null,
                null
        );
    }

    public static LegacyMaterialBatchItemResponse promoted(
            UUID stagingId,
            UUID materialId
    ) {
        return new LegacyMaterialBatchItemResponse(
                stagingId,
                true,
                null,
                materialId
        );
    }

    public static LegacyMaterialBatchItemResponse failure(
            UUID stagingId,
            String message
    ) {
        return new LegacyMaterialBatchItemResponse(
                stagingId,
                false,
                message,
                null
        );
    }
}
