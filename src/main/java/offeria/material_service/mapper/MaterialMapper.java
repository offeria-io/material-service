package offeria.material_service.mapper;

import offeria.material_service.domain.entity.Material;
import offeria.material_service.dto.request.MaterialRequestDTO;
import offeria.material_service.dto.response.MaterialResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper for converting between Entities and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MaterialMapper {

    /**
     * Converts Material entity to MaterialResponseDTO.
     */
    MaterialResponseDTO toResponseDTO(Material material);

    /**
     * Converts MaterialRequestDTO to Material entity.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Material toEntity(MaterialRequestDTO requestDTO);

    /**
     * Updates an existing Material entity from MaterialRequestDTO.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(MaterialRequestDTO requestDTO, @MappingTarget Material material);
}
