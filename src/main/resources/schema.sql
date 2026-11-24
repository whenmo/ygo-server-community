-- -----------------------------------------------------------------------------
-- 建立新表
-- -----------------------------------------------------------------------------
-- 1. CDB 配置檔管理表
CREATE TABLE IF NOT EXISTS cdb_configs (
    id        INTEGER PRIMARY KEY AUTOINCREMENT,
    name      TEXT NOT NULL UNIQUE,
    file_path TEXT NOT NULL UNIQUE,
    pic_cdn   TEXT DEFAULT NULL
)@@

-- 1.1 CDB pic_cdn 備用表
CREATE TABLE IF NOT EXISTS cdb_pic_cdns (
    id      INTEGER PRIMARY KEY AUTOINCREMENT,
    cdb_id  INTEGER NOT NULL,
    pic_cdn TEXT NOT NULL,
    FOREIGN KEY (cdb_id) REFERENCES cdb_configs (id) ON DELETE CASCADE ON UPDATE CASCADE
)@@

-- 2. Users 主表
CREATE TABLE IF NOT EXISTS users (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    name          TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    is_admin      INTEGER DEFAULT 0 NOT NULL CHECK (is_admin IN (0, 1))
)@@

-- 3. Diyers 主表
CREATE TABLE IF NOT EXISTS diyers (
    id      INTEGER PRIMARY KEY AUTOINCREMENT,
    name    TEXT NOT NULL,
    cdb_id  INTEGER NOT NULL,
    user_id INTEGER DEFAULT NULL,
    FOREIGN KEY (cdb_id) REFERENCES cdb_configs (id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
)@@

-- 3.1. 確保當 cdb_id 相同時， name 唯一
CREATE UNIQUE INDEX IF NOT EXISTS idx_diyers_cdb_id_name ON diyers (cdb_id, name)@@

-- 3.2. 確保當 cdb_id 相同時， user_id 唯一
CREATE UNIQUE INDEX IF NOT EXISTS idx_diyers_cdb_id_user_id ON diyers (cdb_id, user_id)@@

-- 4. Diyers 卡號區間表
CREATE TABLE IF NOT EXISTS diyer_code_ranges (
    diyer_id    INTEGER NOT NULL,
    code_range  INTEGER NOT NULL CHECK (code_range BETWEEN 1 AND 2000),
    PRIMARY KEY (diyer_id, code_range),
    FOREIGN KEY (diyer_id) REFERENCES diyers (id) ON DELETE CASCADE ON UPDATE CASCADE
)@@

-- 5. Diyers 字段區間表
CREATE TABLE IF NOT EXISTS diyer_set_ranges (
    diyer_id    INTEGER NOT NULL,
    set_range   INTEGER NOT NULL CHECK (set_range BETWEEN 1 AND 255),
    PRIMARY KEY (diyer_id, set_range),
    FOREIGN KEY (diyer_id) REFERENCES diyers (id) ON DELETE CASCADE ON UPDATE CASCADE
)@@

-- 6. Diyers 字段/指示物/特殊勝利/內置字段/提示表
CREATE TABLE IF NOT EXISTS diyer_strings (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    diyer_id    INTEGER NOT NULL,
    type        TEXT NOT NULL CHECK (type IN ('setname', 'counter', 'victory', 'hint')),
    code        INTEGER DEFAULT NULL CHECK (code BETWEEN 1 AND 65535),
    name        TEXT NOT NULL DEFAULT '???',
    FOREIGN KEY (diyer_id) REFERENCES diyers (id) ON DELETE CASCADE ON UPDATE CASCADE
)@@

-- 7. 卡組卡包表
CREATE TABLE IF NOT EXISTS deck_packs (
    id      INTEGER NOT NULL PRIMARY KEY,
    name    TEXT NOT NULL,
    user_id INTEGER DEFAULT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
)@@

-- 7.1. 確保當 user_id 為 NULL 時，name 唯一
CREATE UNIQUE INDEX IF NOT EXISTS idx_deck_packs_null_user_name
ON deck_packs (name) 
WHERE user_id IS NULL@@

-- 7.2. 確保當 user_id 不為 NULL 時，同一個 user_id 內的 name 唯一
CREATE UNIQUE INDEX IF NOT EXISTS idx_deck_packs_user_name 
ON deck_packs (user_id, name) 
WHERE user_id IS NOT NULL@@

-- 8. 卡組表
CREATE TABLE IF NOT EXISTS decks (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT NOT NULL,
    is_favor    INTEGER DEFAULT 0 NOT NULL CHECK (is_favor IN (0, 1)),
    is_public   INTEGER DEFAULT 0 NOT NULL CHECK (is_public IN (0, 1)),
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    user_id     INTEGER NOT NULL,
    pack_id     INTEGER DEFAULT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (pack_id) REFERENCES deck_packs(id) ON DELETE SET NULL
)@@

-- 8.1. 檢查 INSERT 時是否符合邏輯
CREATE TRIGGER IF NOT EXISTS trg_decks_pack_user_check_insert
BEFORE INSERT ON decks
WHEN NEW.pack_id IS NOT NULL 
  AND NOT EXISTS (
    SELECT 1 FROM deck_packs dp 
    WHERE dp.id = NEW.pack_id 
      AND (dp.user_id IS NULL OR dp.user_id = NEW.user_id)
  )
BEGIN
  SELECT RAISE(ABORT, 'Violation: deck pack user_id must be NULL or match deck user_id');
END@@

-- 8.2. 檢查 UPDATE 時是否符合邏輯
CREATE TRIGGER IF NOT EXISTS trg_decks_pack_user_check_update
BEFORE UPDATE OF user_id, pack_id ON decks
WHEN NEW.pack_id IS NOT NULL 
  AND NOT EXISTS (
    SELECT 1 FROM deck_packs dp 
    WHERE dp.id = NEW.pack_id 
      AND (dp.user_id IS NULL OR dp.user_id = NEW.user_id)
  )
BEGIN
  SELECT RAISE(ABORT, 'Violation: deck pack user_id must be NULL or match deck user_id');
END@@

-- 9. 公告表
CREATE TABLE IF NOT EXISTS boards (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    is_top          INTEGER DEFAULT 0 NOT NULL CHECK (is_top IN (0, 1)),
    title           TEXT NOT NULL,
    content         TEXT,
    edit_user_id    INTEGER NOT NULL,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP
)@@