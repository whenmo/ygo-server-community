-- -----------------------------------------------------------------------------
-- 建立新表
-- -----------------------------------------------------------------------------
-- 1. CDB 配置檔管理表
CREATE TABLE IF NOT EXISTS cdb_configs (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT NOT NULL,
    file_path   TEXT NOT NULL UNIQUE,
    pic_cdn     TEXT NOT NULL
);

-- 2. Users 主表
CREATE TABLE IF NOT EXISTS users (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    name          TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    is_admin      INT  DEFAULT 0 NOT NULL CHECK (is_admin IN (0, 1))
);

-- 3. Diyers 主表
CREATE TABLE IF NOT EXISTS diyers (
    id      INTEGER PRIMARY KEY AUTOINCREMENT,
    name    TEXT NOT NULL UNIQUE,
    user_id INTEGER  DEFAULT NULL UNIQUE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- 4. Diyers 卡號區間表
CREATE TABLE IF NOT EXISTS diyer_code_ranges (
    diyer_id    INTEGER NOT NULL,
    code_range  INT  NOT NULL CHECK (code_range BETWEEN 1 AND 2000),
    PRIMARY KEY (diyer_id, code_range),
    FOREIGN KEY (diyer_id) REFERENCES diyers (id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- 5. Diyers 字段區間表
CREATE TABLE IF NOT EXISTS diyer_set_ranges (
    diyer_id    INTEGER NOT NULL,
    set_range   INT  NOT NULL CHECK (set_range BETWEEN 1 AND 255),
    PRIMARY KEY (diyer_id, set_range),
    FOREIGN KEY (diyer_id) REFERENCES diyers (id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- 6. Diyers 字段/指示物/特殊勝利表
CREATE TABLE IF NOT EXISTS diyer_strings (
    cdb_id      INTEGER NOT NULL,
    diyer_id    INTEGER NOT NULL,
    type        TEXT NOT NULL CHECK (type IN ('setname', 'counter', 'victory', 'internal')),
    code        TEXT NOT NULL,
    name        TEXT NOT NULL,
    PRIMARY KEY (cdb_id, diyer_id, type, code),
    FOREIGN KEY (cdb_id) REFERENCES cdb_configs (id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (diyer_id) REFERENCES diyers (id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- 7. 卡組卡包表
CREATE TABLE IF NOT EXISTS deck_packs (
    name TEXT NOT NULL UNIQUE
);

-- 8. Users 卡組表
CREATE TABLE IF NOT EXISTS user_decks (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id     INTEGER NOT NULL,
    name        TEXT NOT NULL,
    pack_name   TEXT,
    is_public   INT  DEFAULT 0 NOT NULL CHECK (is_public IN (0, 1)),
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);