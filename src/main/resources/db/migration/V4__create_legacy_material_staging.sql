-- Staging area for historical material terminology.
--
-- Raw legacy data is stored here before it is reviewed and promoted
-- into the authoritative materials/material_aliases knowledge base.

CREATE TABLE legacy_material_staging (
                                         id UUID PRIMARY KEY,

                                         raw_value TEXT NOT NULL,

                                         candidate_english_name VARCHAR(500),
                                         candidate_iraqi_name VARCHAR(500),
                                         candidate_standard_arabic_name VARCHAR(500),

                                         normalized_english_name VARCHAR(500),
                                         normalized_iraqi_name VARCHAR(500),

                                         source_workbook VARCHAR(255) NOT NULL,
                                         source_sheet VARCHAR(255) NOT NULL,
                                         source_row INTEGER,
                                         source_column VARCHAR(100),

                                         status VARCHAR(50) NOT NULL,

                                         review_notes TEXT,

                                         imported_material_id UUID,

                                         created_at TIMESTAMP NOT NULL,
                                         updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_legacy_material_staging_status
    ON legacy_material_staging (status);

CREATE INDEX idx_legacy_material_staging_source
    ON legacy_material_staging (source_workbook, source_sheet);