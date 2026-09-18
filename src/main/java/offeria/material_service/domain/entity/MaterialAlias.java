package offeria.material_service.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import offeria.material_service.domain.enums.AliasLanguage;
import offeria.material_service.domain.enums.AliasType;
import offeria.material_service.domain.enums.MaterialSource;
import offeria.material_service.domain.enums.MaterialStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Alternative known name for a canonical Material.
 *
 * Stores terminology collected from market usage, suppliers,
 * legacy data, RFQs, and other knowledge sources.
 */
@Entity
@Table(name = "material_aliases", indexes = {
        @Index(name = "idx_material_alias_material_id", columnList = "material_id"),
        @Index(name = "idx_material_alias_alias", columnList = "alias"),
        @Index(name = "idx_material_alias_normalized_alias", columnList = "normalized_alias"),
        @Index(name = "idx_material_alias_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialAlias {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(name = "alias", nullable = false, length = 255)
    private String alias;

    @Column(name = "normalized_alias", length = 255)
    private String normalizedAlias;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", nullable = false, length = 50)
    private AliasLanguage language;

    @Enumerated(EnumType.STRING)
    @Column(name = "alias_type", nullable = false, length = 50)
    private AliasType aliasType;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 50)
    private MaterialSource source;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private MaterialStatus status;

    @Column(name = "preferred", nullable = false)
    private boolean preferred;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}