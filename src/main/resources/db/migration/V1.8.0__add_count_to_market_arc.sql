ALTER TABLE market_arc
    ADD COLUMN count INTEGER NOT NULL DEFAULT 0
        CONSTRAINT chk_market_count_non_negative CHECK (count >= 0);

CREATE TABLE market_arc_hist (
    id                 UUID         PRIMARY KEY,
    market_arc_id      UUID         NOT NULL,
    event_type         VARCHAR(20)  NOT NULL,
    brand_id           UUID         NOT NULL,
    tabacoo_flavor_id  UUID         NOT NULL,
    name               CITEXT       NOT NULL,
    weight_grams       INTEGER      NOT NULL,
    count              INTEGER      NOT NULL,
    gtin               VARCHAR(32),
    created_at         TIMESTAMPTZ  NOT NULL,
    updated_at         TIMESTAMPTZ  NOT NULL,
    updated_by         UUID         NOT NULL
);

CREATE INDEX idx_market_hist_arc_id    ON market_arc_hist(market_arc_id);
CREATE INDEX idx_market_hist_updated_by ON market_arc_hist(updated_by);
