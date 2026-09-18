-- Create alternative terminology for canonical materials.

CREATE TABLE material_aliases (
                                  id UUID PRIMARY KEY,
                                  material_id UUID NOT NULL,
                                  alias VARCHAR(255) NOT NULL,
                                  normalized_alias VARCHAR(255),
                                  language VARCHAR(50) NOT NULL,
                                  alias_type VARCHAR(50) NOT NULL,
                                  source VARCHAR(50) NOT NULL,
                                  status VARCHAR(50) NOT NULL,
                                  preferred BOOLEAN NOT NULL DEFAULT FALSE,
                                  created_at TIMESTAMP,
                                  updated_at TIMESTAMP,

                                  CONSTRAINT fk_material_alias_material
                                      FOREIGN KEY (material_id)
                                          REFERENCES materials (id)
);

-- Support material-to-alias navigation and future alias lookup.

CREATE INDEX idx_material_alias_material_id
    ON material_aliases (material_id);

CREATE INDEX idx_material_alias_alias
    ON material_aliases (alias);

CREATE INDEX idx_material_alias_normalized_alias
    ON material_aliases (normalized_alias);

CREATE INDEX idx_material_alias_status
    ON material_aliases (status);