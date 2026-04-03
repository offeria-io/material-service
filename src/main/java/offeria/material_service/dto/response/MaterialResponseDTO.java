package offeria.material_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for returning Material details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialResponseDTO {
    private UUID id;
    private String nameEn;
    private String nameAr;
    private String unit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
