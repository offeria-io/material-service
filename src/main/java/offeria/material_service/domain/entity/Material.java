package offeria.material_service.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import offeria.material_service.domain.enums.MaterialSource;
import offeria.material_service.domain.enums.MaterialStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Canonical Material entity for the Offeria Material Knowledge Base.
 *
 * Stores material identity, Iraqi-market terminology, classification,
 * review status, and knowledge source.
 */
@Entity
@Table(name = "materials", indexes = {
        @Index(name = "idx_material_canonical_english_name", columnList = "canonical_english_name"),
        @Index(name = "idx_material_preferred_iraqi_name", columnList = "preferred_iraqi_name"),
        @Index(name = "idx_material_normalized_english_name", columnList = "normalized_english_name"),
        @Index(name = "idx_material_normalized_iraqi_name", columnList = "normalized_iraqi_name"),
        @Index(name = "idx_material_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "canonical_english_name", nullable = false, length = 255)
    private String canonicalEnglishName;

    @Column(name = "preferred_iraqi_name", length = 255)
    private String preferredIraqiName;

    @Column(name = "standard_arabic_name", length = 255)
    private String standardArabicName;

    @Column(name = "normalized_english_name", length = 255)
    private String normalizedEnglishName;

    @Column(name = "normalized_iraqi_name", length = 255)
    private String normalizedIraqiName;

    @Column(name = "unit", nullable = false, length = 50)
    private String unit;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "sub_category", length = 100)
    private String subCategory;

    @Column(name = "manufacturer", length = 150)
    private String manufacturer;

    @Column(name = "brand", length = 150)
    private String brand;

    @Column(name = "part_number", length = 150)
    private String partNumber;

    @Column(name = "specification", length = 1000)
    private String specification;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private MaterialStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 50)
    private MaterialSource source;

    @OneToMany(mappedBy = "material")
    @Builder.Default
    private List<MaterialAlias> aliases = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
