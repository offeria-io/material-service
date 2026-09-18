package offeria.material_service.repository;

import offeria.material_service.domain.entity.MaterialAlias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MaterialAliasRepository
        extends JpaRepository<MaterialAlias, UUID> {
}
