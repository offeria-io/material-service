package offeria.material_service.repository;

import offeria.material_service.domain.entity.LegacyMaterialStaging;
import offeria.material_service.domain.enums.LegacyMaterialImportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LegacyMaterialStagingRepository
        extends JpaRepository<LegacyMaterialStaging, UUID> {

    List<LegacyMaterialStaging> findByStatus(
            LegacyMaterialImportStatus status
    );

    List<LegacyMaterialStaging> findBySourceWorkbookAndSourceSheet(
            String sourceWorkbook,
            String sourceSheet
    );
}