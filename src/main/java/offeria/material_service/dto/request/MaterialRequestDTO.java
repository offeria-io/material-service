package offeria.material_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating a Material.
 * Includes validation logic to ensure data integrity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialRequestDTO {

    @NotBlank(message = "English name is required")
    @Size(min = 2, max = 255, message = "English name must be between 2 and 255 characters")
    private String nameEn;


    @Size(min = 2, max = 255, message = "Arabic name must be between 2 and 255 characters")
    private String nameAr;

    @NotBlank(message = "Unit is required")
    @Size(max = 50, message = "Unit cannot exceed 50 characters")
    private String unit;
}
