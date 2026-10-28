// 規則
const RULE = {
    OCG: 0x1,
    TCG: 0x2,
    OTCG: 0x3,
    CUSTOM: 0x4,
    CCG: 0x8,
};

const RULE_CN = {
    [RULE.OCG]: 'OCG',
    [RULE.TCG]: 'TCG',
    [RULE.OTCG]: 'O&T',
    [RULE.CUSTOM]: '自定義',
    [RULE.CCG]: '簡中'
};

// 卡片類型常數
const TYPE_MAIN = {
    MONSTER: 0x1,
    SPELL: 0x2,
    TRAP: 0x4,
};

const TYPE_MONSTER = {
    NORMAL: 0x10,
    EFFECT: 0x20,
    FUSION: 0x40,
    RITUAL: 0x80,
    SPIRIT: 0x200,
    UNION: 0x400,
    DUAL: 0x800,
    TUNER: 0x1000,
    SYNCHRO: 0x2000,
    TOKEN: 0x4000,
    FLIP: 0x200000,
    TOON: 0x400000,
    XYZ: 0x800000,
    PENDULUM: 0x1000000,
    SPSUMMON: 0x2000000,
    LINK: 0x4000000
};

const TYPE_SPELL_TRAP = {
    NORMAL: 0x10,
    RITUAL: 0x80,
    QUICKPLAY: 0x10000,
    CONTINUOUS: 0x20000,
    EQUIP: 0x40000,
    FIELD: 0x80000,
    COUNTER: 0x100000
};

const TYPE = {
    MAIN: 0x7,
    ST: 0x6,
    ...TYPE_MAIN,
    ...TYPE_MONSTER,
    ...TYPE_SPELL_TRAP,
};

const TYPE_CN = {
    0x1: '怪獸',
    0x2: '魔法',
    0x4: '陷阱',
    0x10: '通常',
    0x20: '效果',
    0x40: '融合',
    0x80: '儀式',
    0x200: '靈魂',
    0x400: '同盟',
    0x800: '二重',
    0x1000: '調整',
    0x2000: '同步',
    0x4000: '衍生物',
    0x10000: '速攻',
    0x20000: '永續',
    0x40000: '裝備',
    0x80000: '場地',
    0x100000: '反擊',
    0x200000: '反轉',
    0x400000: '卡通',
    0x800000: '超量',
    0x1000000: '靈擺',
    0x2000000: '特殊召喚',
    0x4000000: '連接'
};

// attribute
const ATTRIBUTE = {
    EARTH: 0x1,
    WATER: 0x2,
    FIRE: 0x4,
    WIND: 0x8,
    LIGHT: 0x10,
    DARK: 0x20,
    DIVINE: 0x40,
};

const ATTRIBUTE_CN = {
    [ATTRIBUTE.EARTH]: '地',
    [ATTRIBUTE.WATER]: '水',
    [ATTRIBUTE.FIRE]: '炎',
    [ATTRIBUTE.WIND]: '風',
    [ATTRIBUTE.LIGHT]: '光',
    [ATTRIBUTE.DARK]: '暗',
    [ATTRIBUTE.DIVINE]: '神'
};

// Race
const RACE = {
    WARRIOR: 0x1,
    SPELLCASTER: 0x2,
    FAIRY: 0x4,
    FIEND: 0x8,
    ZOMBIE: 0x10,
    MACHINE: 0x20,
    AQUA: 0x40,
    PYRO: 0x80,
    ROCK: 0x100,
    WINGED_BEAST: 0x200,
    PLANT: 0x400,
    INSECT: 0x800,
    THUNDER: 0x1000,
    DRAGON: 0x2000,
    BEAST: 0x4000,
    BEAST_WARRIOR: 0x8000,
    DINOSAUR: 0x10000,
    FISH: 0x20000,
    SEA_SERPENT: 0x40000,
    REPTILE: 0x80000,
    PSYCHIC: 0x100000,
    DIVINE_BEAST: 0x200000,
    CREATOR_GOD: 0x400000,
    WYRM: 0x800000,
    CYBERSE: 0x1000000,
    ILLUSION: 0x2000000,
};

const RACE_CN = {
    [RACE.WARRIOR]: '戰士',
    [RACE.SPELLCASTER]: '魔法師',
    [RACE.FAIRY]: '天使',
    [RACE.FIEND]: '惡魔',
    [RACE.ZOMBIE]: '不死',
    [RACE.MACHINE]: '機械',
    [RACE.AQUA]: '水',
    [RACE.PYRO]: '炎',
    [RACE.ROCK]: '岩石',
    [RACE.WINGED_BEAST]: '鳥獸',
    [RACE.PLANT]: '植物',
    [RACE.INSECT]: '昆蟲',
    [RACE.THUNDER]: '雷',
    [RACE.DRAGON]: '龍',
    [RACE.BEAST]: '獸',
    [RACE.BEAST_WARRIOR]: '獸戰士',
    [RACE.DINOSAUR]: '恐龍',
    [RACE.FISH]: '魚',
    [RACE.SEA_SERPENT]: '海龍',
    [RACE.REPTILE]: '爬蟲類',
    [RACE.PSYCHIC]: '念動力',
    [RACE.DIVINE_BEAST]: '幻神獸',
    [RACE.CREATOR_GOD]: '創造神',
    [RACE.WYRM]: '幻龍',
    [RACE.CYBERSE]: '電子界',
    [RACE.ILLUSION]: '幻想魔'
};

// Link Marker
const LINK = {
    BOTTOM_LEFT: 0x1,
    BOTTOM: 0x2,
    BOTTOM_RIGHT: 0x4,
    LEFT: 0x8,
    RIGHT: 0x20,
    TOP_LEFT: 0x40,
    TOP: 0x80,
    TOP_RIGHT: 0x100,
};

const CATEGORY = {
    SPELL_TRAP_DESTROY: 0x1,
    MONSTER_DESTROY: 0x2,
    BANISH: 0x4,
    TO_GRAVEYARD: 0x8,
    TO_HAND: 0x10,
    TO_DECK: 0x20,
    HAND_DESTRUCTION: 0x40,
    DECK_DESTRUCTION: 0x80,
    DRAW: 0x100,
    SEARCH: 0x200,
    RECYCLE: 0x400,
    POSITION_CHANGE: 0x800,
    CONTROL: 0x1000,
    ATK_DEF_CHANGE: 0x2000,
    PIERCE: 0x4000,
    MULTI_ATTACK: 0x8000,
    ATTACK_LIMIT: 0x10000,
    DIRECT_ATTACK: 0x20000,
    SPECIAL_SUMMON: 0x40000,
    TOKEN: 0x80000,
    RACE_RELATED: 0x100000,
    ATTRIBUTE_RELATED: 0x200000,
    LP_DAMAGE: 0x400000,
    LP_RECOVERY: 0x800000,
    DESTROY_RESISTANCE: 0x1000000,
    EFFECT_RESISTANCE: 0x2000000,
    COUNTER: 0x4000000,
    GAMBLE: 0x8000000,
    FUSION_RELATED: 0x10000000,
    SYNCHRO_RELATED: 0x20000000,
    XYZ_RELATED: 0x40000000,
    DISABLE_EFFECT: 0x80000000,
};

const CATEGORY_CN = {
    [CATEGORY.SPELL_TRAP_DESTROY]: '魔陷破壞',
    [CATEGORY.MONSTER_DESTROY]: '怪獸破壞',
    [CATEGORY.BANISH]: '卡片除外',
    [CATEGORY.TO_GRAVEYARD]: '送去墓地',
    [CATEGORY.TO_HAND]: '返回手卡',
    [CATEGORY.TO_DECK]: '返回卡組',
    [CATEGORY.HAND_DESTRUCTION]: '手卡破壞',
    [CATEGORY.DECK_DESTRUCTION]: '卡組破壞',
    [CATEGORY.DRAW]: '抽卡輔助',
    [CATEGORY.SEARCH]: '卡組檢索',
    [CATEGORY.RECYCLE]: '卡片回收',
    [CATEGORY.POSITION_CHANGE]: '表示形式',
    [CATEGORY.CONTROL]: '控制權',
    [CATEGORY.ATK_DEF_CHANGE]: '攻守變化',
    [CATEGORY.PIERCE]: '穿刺傷害',
    [CATEGORY.MULTI_ATTACK]: '多次攻擊',
    [CATEGORY.ATTACK_LIMIT]: '攻擊限制',
    [CATEGORY.DIRECT_ATTACK]: '直接攻擊',
    [CATEGORY.SPECIAL_SUMMON]: '特殊召喚',
    [CATEGORY.TOKEN]: '衍生物',
    [CATEGORY.RACE_RELATED]: '種族相關',
    [CATEGORY.ATTRIBUTE_RELATED]: '屬性相關',
    [CATEGORY.LP_DAMAGE]: 'LP傷害',
    [CATEGORY.LP_RECOVERY]: 'LP回覆',
    [CATEGORY.DESTROY_RESISTANCE]: '破壞耐性',
    [CATEGORY.EFFECT_RESISTANCE]: '效果耐性',
    [CATEGORY.COUNTER]: '指示物',
    [CATEGORY.GAMBLE]: '賭博相關',
    [CATEGORY.FUSION_RELATED]: '融合相關',
    [CATEGORY.SYNCHRO_RELATED]: '同調相關',
    [CATEGORY.XYZ_RELATED]: '超量相關',
    [CATEGORY.DISABLE_EFFECT]: '效果無效'
};

window.APP_CONSTANTS = {
    RULE,
    RULE_CN,
    TYPE_MAIN,
    TYPE_MONSTER,
    TYPE_SPELL_TRAP,
    TYPE,
    TYPE_CN,
    ATTRIBUTE,
    ATTRIBUTE_CN,
    RACE,
    RACE_CN,
    LINK,
    CATEGORY,
    CATEGORY_CN,
};