-- Make flavor_id nullable in flavor_pack and change FK to ON DELETE SET NULL

ALTER TABLE flavor_pack ALTER COLUMN flavor_id DROP NOT NULL;

ALTER TABLE flavor_pack DROP CONSTRAINT fk_pack_flavor;

ALTER TABLE flavor_pack
    ADD CONSTRAINT fk_pack_flavor
        FOREIGN KEY (flavor_id)
        REFERENCES tabacoo_flavor(id)
        ON DELETE SET NULL;
