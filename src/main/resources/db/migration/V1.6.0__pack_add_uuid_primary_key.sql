-- 1. Переименовать существующий id (VARCHAR) → tag_id (до добавления нового id)
ALTER TABLE flavor_pack RENAME COLUMN id TO tag_id;

-- 2. Удалить старый PRIMARY KEY (теперь на tag_id)
ALTER TABLE flavor_pack DROP CONSTRAINT flavor_pack_pkey;

-- 3. Добавить новое поле id UUID
ALTER TABLE flavor_pack ADD COLUMN id UUID;

-- 4. Проставить UUID для существующих строк
UPDATE flavor_pack SET id = gen_random_uuid();

-- 5. Сделать поле NOT NULL
ALTER TABLE flavor_pack ALTER COLUMN id SET NOT NULL;

-- 6. Назначить новый PRIMARY KEY
ALTER TABLE flavor_pack ADD PRIMARY KEY (id);

-- 7. Добавить UNIQUE на tag_id
ALTER TABLE flavor_pack ADD CONSTRAINT uq_pack_tag_id UNIQUE (tag_id);

-- 8. Индекс на tag_id
CREATE INDEX idx_pack_tag_id ON flavor_pack(tag_id);
