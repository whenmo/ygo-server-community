// 規則
const RULE = {
    OCG: 0x1,
    TCG: 0x2,
    OTCG: 0x3,
    CUSTOM: 0x4,
    CCG: 0x8,
};

// 卡片類型常數
const TYPE = {
    MAIN: 0x7,
    ST: 0x6,
    MONSTER: 0x1,
    SPELL: 0x2,
    TRAP: 0x4,
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
    QUICKPLAY: 0x10000,
    CONTINUOUS: 0x20000,
    EQUIP: 0x40000,
    FIELD: 0x80000,
    COUNTER: 0x100000,
    FLIP: 0x200000,
    TOON: 0x400000,
    XYZ: 0x800000,
    PENDULUM: 0x1000000,
    SPSUMMON: 0x2000000,
    LINK: 0x4000000,
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

// function
const sortCards = (a, b) => {
    const typeA = Number(a.type || 0);
    const typeB = Number(b.type || 0);

    let sortResult = (typeA & TYPE.MAIN) - (typeB & TYPE.MAIN);
    if (sortResult !== 0) return sortResult;

    sortResult = Number(b['type'] || 0) - Number(a['type'] || 0);
    if (sortResult !== 0) return sortResult;

    if (typeA & TYPE.MONSTER > 0) {
        const sortFields = ['level', 'atk', 'def', 'attribute', 'race'];
        for (const field of sortFields) {
            sortResult = Number(b[field] || 0) - Number(a[field] || 0);
            if (sortResult !== 0) return sortResult;
        }
    }

    return Number(b['id'] || 0) - Number(a['id'] || 0);
};

const isRule = (card, code) => {
    // special code : 0 (N/A), -1 (other)
    if (code === 0) return true;
    const cardRule = Number(card?.rule || 0);
    if (code === -1) {
        if (cardRule === 0) return true;
        if (cardRule & RULE.CUSTOM) return (cardRule | RULE.CUSTOM) !== RULE.CUSTOM;
        return false
    }
    if (code === RULE.OCG) return (cardRule & RULE.OCG) && !(cardRule & RULE.TCG);
    if (code === RULE.TCG) return (cardRule & RULE.TCG) && !(cardRule & RULE.OCG);
    return (cardRule & code) === code
};

const hasType = (card, code) => {
    const type = Number(card?.type || 0);
    const target = Number(code || 0);
    if (type === 0 || target === 0) return false;
    return (type & target) === target;
};

const isType = {
    Monster: (card) => hasType(card, TYPE.MONSTER),
    Spell: (card) => hasType(card, TYPE.SPELL),
    Trap: (card) => hasType(card, TYPE.TRAP),
    Xyz: (card) => hasType(card, TYPE.XYZ),
    Link: (card) => hasType(card, TYPE.LINK),
    Pendulum: (card) => hasType(card, TYPE.PENDULUM),
    Extra: (card) => hasType(card, TYPE.FUSION | TYPE.SYNCHRO | TYPE.XYZ | TYPE.LINK),
};

const isSetCard = (card, code) => {
    const cardSets = Array.isArray(card?.setcode) ? card.setcode : [];
    const target = Number(code || 0);
    const typeMain = target & 0x0FFF;
    const typeSub = target & 0xF000;
    return cardSets.some(cardCode => (cardCode & 0x0FFF) === typeMain && (cardCode & typeSub) === typeSub);
};

const formatAtkDefValue = (value) => (value === -2) ? '?' : Number(value).toLocaleString('zh-Hans');

const getCardAtkDef = (card) => `${formatAtkDefValue(card?.atk)}${isType.Link(card) ? '' : `/${formatAtkDefValue(card?.def)}`}`;

const getCardLimitStatus = (card, iflistMap) => {
    if (!iflistMap || Object.keys(iflistMap).length === 0) return null;
    const id = Number(card?.id || 0);
    for (const [statusCode, items] of Object.entries(iflistMap)) {
        if (Array.isArray(items) && items.some(item => Number(item.id) === id)) {
            const code = Number(statusCode);
            if (code >= 0 && code <= 2) return code;
        }
    }
    return null;
};

window.APP_CONSTANTS = {
    RULE,
    TYPE,
    ATTRIBUTE,
    LINK,
    sortCards,
    isRule,
    hasType,
    isType,
    isSetCard,
    formatAtkDefValue,
    getCardAtkDef,
    getCardLimitStatus,
};