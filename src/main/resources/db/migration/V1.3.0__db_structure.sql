-- =========================================================
-- EXTENSIONS
-- =========================================================

CREATE EXTENSION IF NOT EXISTS citext;



-- =========================================================
-- UPDATED_AT TRIGGER
-- =========================================================

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;



-- =========================================================
-- TABACOO BRAND
-- =========================================================

CREATE TABLE tabacoo_brand (
    id UUID PRIMARY KEY,

    name CITEXT NOT NULL,
    description VARCHAR(1000),

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by UUID NOT NULL
);

CREATE UNIQUE INDEX ux_tabacoo_brand_name
ON tabacoo_brand(name);



-- =========================================================
-- TABACOO FLAVOR
-- =========================================================

CREATE TABLE tabacoo_flavor (
    id UUID PRIMARY KEY,

    brand_id UUID NOT NULL,

    name CITEXT NOT NULL,
    description VARCHAR(1000),

    strength SMALLINT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by UUID NOT NULL,

    CONSTRAINT fk_flavor_brand
        FOREIGN KEY (brand_id)
        REFERENCES tabacoo_brand(id)
        ON DELETE CASCADE,

    CONSTRAINT ux_flavor_brand_name
        UNIQUE (brand_id, name),

    CONSTRAINT chk_strength_range
        CHECK (strength BETWEEN 0 AND 10)
);

CREATE INDEX idx_flavor_brand
ON tabacoo_flavor(brand_id);



-- =========================================================
-- FLAVOR PACK
-- =========================================================

CREATE TABLE flavor_pack (
    id VARCHAR(100) PRIMARY KEY,

    flavor_id UUID NOT NULL,

    current_weight_grams INTEGER NOT NULL,
    total_weight_grams INTEGER NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by UUID NOT NULL,

    CONSTRAINT fk_pack_flavor
        FOREIGN KEY (flavor_id)
        REFERENCES tabacoo_flavor(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_pack_weight
        CHECK (
            total_weight_grams > 0
            AND current_weight_grams >= 0
            AND current_weight_grams <= total_weight_grams
        )
);

CREATE INDEX idx_pack_flavor
ON flavor_pack(flavor_id);



-- =========================================================
-- MARKET ARC
-- =========================================================

CREATE TABLE market_arc (
    id UUID PRIMARY KEY,

    brand_id UUID NOT NULL,
    tabacoo_flavor_id UUID NOT NULL,

    name CITEXT NOT NULL,

    weight_grams INTEGER NOT NULL,
    gtin VARCHAR(32),

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by UUID NOT NULL,

    CONSTRAINT fk_market_brand
        FOREIGN KEY (brand_id)
        REFERENCES tabacoo_brand(id),

    CONSTRAINT fk_market_flavor
        FOREIGN KEY (tabacoo_flavor_id)
        REFERENCES tabacoo_flavor(id),

    CONSTRAINT chk_weight_positive
        CHECK (weight_grams > 0)
);

CREATE INDEX idx_market_brand
ON market_arc(brand_id);

CREATE INDEX idx_market_flavor
ON market_arc(tabacoo_flavor_id);

CREATE UNIQUE INDEX ux_market_gtin
ON market_arc(gtin)
WHERE gtin IS NOT NULL;



-- =========================================================
-- BRAND TAGS
-- =========================================================

CREATE TABLE tabacoo_brand_tags (
    tabacoo_brand_id UUID NOT NULL,
    tag_id UUID NOT NULL,

    PRIMARY KEY (tabacoo_brand_id, tag_id),

    CONSTRAINT fk_brand_tag_brand
        FOREIGN KEY (tabacoo_brand_id)
        REFERENCES tabacoo_brand(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_brand_tag_tag
        FOREIGN KEY (tag_id)
        REFERENCES tags(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_brand_tags_tag
ON tabacoo_brand_tags(tag_id);



-- =========================================================
-- FLAVOR TAGS
-- =========================================================

CREATE TABLE flavor_tags (
    tabacoo_flavor_id UUID NOT NULL,
    tag_id UUID NOT NULL,

    PRIMARY KEY (tabacoo_flavor_id, tag_id),

    CONSTRAINT fk_flavor_tag_flavor
        FOREIGN KEY (tabacoo_flavor_id)
        REFERENCES tabacoo_flavor(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_flavor_tag_tag
        FOREIGN KEY (tag_id)
        REFERENCES tags(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_flavor_tags_tag
ON flavor_tags(tag_id);



-- =========================================================
-- TRIGGERS
-- =========================================================

CREATE TRIGGER trg_brand_updated
BEFORE UPDATE ON tabacoo_brand
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_flavor_updated
BEFORE UPDATE ON tabacoo_flavor
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_pack_updated
BEFORE UPDATE ON flavor_pack
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_market_updated
BEFORE UPDATE ON market_arc
FOR EACH ROW EXECUTE FUNCTION set_updated_at();