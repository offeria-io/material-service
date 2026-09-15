-- Rename existing material-name columns to the new domain terminology.
ALTER TABLE materials
    RENAME COLUMN name_en TO canonical_english_name;

ALTER TABLE materials
    RENAME COLUMN name_ar TO preferred_iraqi_name;

-- Iraqi-market terminology is optional until a material is reviewed.
ALTER TABLE materials
    ALTER COLUMN preferred_iraqi_name DROP NOT NULL;

-- Align the existing unit column with the JPA model.
ALTER TABLE materials
ALTER COLUMN unit TYPE VARCHAR(50);

-- Add Material Knowledge Base fields.
ALTER TABLE materials
    ADD COLUMN standard_arabic_name VARCHAR(255),
    ADD COLUMN normalized_english_name VARCHAR(255),
    ADD COLUMN normalized_iraqi_name VARCHAR(255),
    ADD COLUMN category VARCHAR(100),
    ADD COLUMN sub_category VARCHAR(100),
    ADD COLUMN manufacturer VARCHAR(150),
    ADD COLUMN brand VARCHAR(150),
    ADD COLUMN part_number VARCHAR(150),
    ADD COLUMN specification VARCHAR(1000),
    ADD COLUMN status VARCHAR(50),
    ADD COLUMN source VARCHAR(50);

-- Existing records predate the review lifecycle.
-- Treat them as approved manual knowledge so the migration is backward compatible.
UPDATE materials
SET status = 'APPROVED'
WHERE status IS NULL;

UPDATE materials
SET source = 'MANUAL'
WHERE source IS NULL;

ALTER TABLE materials
    ALTER COLUMN status SET NOT NULL,
ALTER COLUMN source SET NOT NULL;

-- Replace legacy indexes with indexes matching the new domain model.
DROP INDEX idx_material_name_en;
DROP INDEX idx_material_name_ar;

CREATE INDEX idx_material_canonical_english_name
    ON materials (canonical_english_name);

CREATE INDEX idx_material_preferred_iraqi_name
    ON materials (preferred_iraqi_name);

CREATE INDEX idx_material_normalized_english_name
    ON materials (normalized_english_name);

CREATE INDEX idx_material_normalized_iraqi_name
    ON materials (normalized_iraqi_name);

CREATE INDEX idx_material_status
    ON materials (status);
