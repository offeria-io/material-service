package offeria.material_service.mapper;

import offeria.material_service.domain.entity.Material;
import offeria.material_service.dto.request.MaterialRequestDTO;
import offeria.material_service.dto.response.MaterialResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper for converting between Material entities and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MaterialMapper {

    /**
     * Converts Material entity to MaterialResponseDTO while preserving
     * the existing API field names.
     */
    @Mapping(source = "canonicalEnglishName", target = "nameEn")
    @Mapping(source = "preferredIraqiName", target = "nameAr")
    MaterialResponseDTO toResponseDTO(Material material);

    /**
     * Converts MaterialRequestDTO to Material entity while preserving
     * backward compatibility with the existing API contract.
     */
    @Mapping(source = "nameEn", target = "canonicalEnglishName")
    @Mapping(source = "nameAr", target = "preferredIraqiName")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "source", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Material toEntity(MaterialRequestDTO requestDTO);

    /**
     * Updates an existing Material entity from MaterialRequestDTO.
     */
    @Mapping(source = "nameEn", target = "canonicalEnglishName")
    @Mapping(source = "nameAr", target = "preferredIraqiName")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "source", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(MaterialRequestDTO requestDTO, @MappingTarget Material material);
}
