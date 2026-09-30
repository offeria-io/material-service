package offeria.material_service.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import offeria.material_service.domain.enums.LegacyMaterialImportStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "legacy_material_staging",
        indexes = {
                @Index(
                        name = "idx_legacy_material_staging_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_legacy_material_staging_source",
                        columnList = "source_workbook, source_sheet"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LegacyMaterialStaging {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /*
     * Original legacy value exactly as received from the source.
     * This value must never be normalized or modified.
     */
    @Column(name = "raw_value", nullable = false, columnDefinition = "TEXT")
    private String rawValue;

    /*
     * Candidate values extracted from raw legacy data.
     * They are not authoritative Material Knowledge Base values.
     */
    @Column(name = "candidate_english_name", length = 500)
    private String candidateEnglishName;

    @Column(name = "candidate_iraqi_name", length = 500)
    private String candidateIraqiName;

    @Column(name = "candidate_standard_arabic_name", length = 500)
    private String candidateStandardArabicName;

    @Column(name = "normalized_english_name", length = 500)
    private String normalizedEnglishName;

    @Column(name = "normalized_iraqi_name", length = 500)
    private String normalizedIraqiName;

    /*
     * Traceability back to the original legacy source.
     */
    @Column(name = "source_workbook", nullable = false, length = 255)
    private String sourceWorkbook;

    @Column(name = "source_sheet", nullable = false, length = 255)
    private String sourceSheet;

    @Column(name = "source_row")
    private Integer sourceRow;

    @Column(name = "source_column", length = 100)
    private String sourceColumn;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private LegacyMaterialImportStatus status;

    /*
     * Optional review information.
     */
    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;

    /*
     * Filled only after the staging record has been promoted
     * into the authoritative Material Knowledge Base.
     *
     * No JPA relationship is intentionally used here.
     * Staging data must remain isolated from normal material lookup.
     */
    @Column(name = "imported_material_id")
    private UUID importedMaterialId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}