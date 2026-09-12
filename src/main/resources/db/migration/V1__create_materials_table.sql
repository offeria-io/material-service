CREATE TABLE materials (
                           id UUID NOT NULL,
                           name_en VARCHAR(255) NOT NULL,
                           name_ar VARCHAR(255) NOT NULL,
                           unit VARCHAR(255) NOT NULL,
                           created_at TIMESTAMP(6),
                           updated_at TIMESTAMP(6),

                           CONSTRAINT pk_materials PRIMARY KEY (id)
);

CREATE INDEX idx_material_name_en
    ON materials (name_en);

CREATE INDEX idx_material_name_ar
    ON materials (name_ar);
