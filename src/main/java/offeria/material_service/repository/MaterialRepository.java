package offeria.material_service.repository;

import offeria.material_service.domain.entity.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for Material entity.
 * Extends JpaRepository for standard CRUD and JpaSpecificationExecutor for advanced filtering.
 */
@Repository
public interface MaterialRepository extends JpaRepository<Material, UUID>, JpaSpecificationExecutor<Material> {

    /**
     * Search materials by canonical English name or preferred Iraqi-market name
     * using case-insensitive partial matching.
     *
     * This preserves the existing API search behavior while the dedicated
     * Material Knowledge Base search strategy is implemented separately.
     */
    @Query("SELECT m FROM Material m WHERE " +
            "LOWER(m.canonicalEnglishName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(m.preferredIraqiName) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Material> searchByName(@Param("query") String query, Pageable pageable);
}
