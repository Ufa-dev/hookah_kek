-- =========================================================
-- TABACOO BRAND HISTORY
-- =========================================================

CREATE TABLE tabacoo_brand_hist (
    id UUID PRIMARY KEY,
    tabacoo_brand_id UUID NOT NULL,
    event_type VARCHAR(20) NOT NULL,
    name CITEXT NOT NULL,
    description VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by UUID NOT NULL
);

CREATE INDEX idx_brand_hist_brand_id   ON tabacoo_brand_hist(tabacoo_brand_id);
CREATE INDEX idx_brand_hist_updated_by ON tabacoo_brand_hist(updated_by);


-- =========================================================
-- TABACOO FLAVOR HISTORY
-- =========================================================

CREATE TABLE tabacoo_flavor_hist (
    id UUID PRIMARY KEY,
    tabacoo_flavor_id UUID NOT NULL,
    event_type VARCHAR(20) NOT NULL,
    brand_id UUID NOT NULL,
    name CITEXT NOT NULL,
    description VARCHAR(1000),
    strength SMALLINT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by UUID NOT NULL
);

CREATE INDEX idx_flavor_hist_flavor_id  ON tabacoo_flavor_hist(tabacoo_flavor_id);
CREATE INDEX idx_flavor_hist_updated_by ON tabacoo_flavor_hist(updated_by);


-- =========================================================
-- FLAVOR PACK HISTORY
-- =========================================================

CREATE TABLE flavor_pack_hist (
    id UUID PRIMARY KEY,
    flavor_pack_id UUID NOT NULL,
    event_type VARCHAR(20) NOT NULL,
    tag_id VARCHAR(100) NOT NULL,
    name VARCHAR(200) NOT NULL,
    flavor_id UUID,
    current_weight_grams INT NOT NULL,
    total_weight_grams INT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by UUID NOT NULL
);

CREATE INDEX idx_pack_hist_pack_id     ON flavor_pack_hist(flavor_pack_id);
CREATE INDEX idx_pack_hist_updated_by  ON flavor_pack_hist(updated_by);
