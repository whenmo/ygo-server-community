(function (window) {
    const { RULE, TYPE } = window.APP_CONSTANTS || {};

    // 卡片排序邏輯
    const sortCards = (a, b) => {
        const typeA = Number(a.type || 0);
        const typeB = Number(b.type || 0);

        let sortResult = (typeA & TYPE.MAIN) - (typeB & TYPE.MAIN);
        if (sortResult !== 0) return sortResult;

        sortResult = Number(b['type'] || 0) - Number(a['type'] || 0);
        if (sortResult !== 0) return sortResult;

        if ((typeA & TYPE.MONSTER) > 0) {
            const sortFields = ['level', 'atk', 'def', 'attribute', 'race'];
            for (const field of sortFields) {
                sortResult = Number(b[field] || 0) - Number(a[field] || 0);
                if (sortResult !== 0) return sortResult;
            }
        }

        return Number(b['id'] || 0) - Number(a['id'] || 0);
    };

    // 規則判斷
    const isRule = (card, code) => {
        if (code === 0) return true;
        const cardRule = Number(card?.rule || 0);
        if (code === -1) {
            if (cardRule === 0) return true;
            if (cardRule & RULE.CUSTOM) return (cardRule | RULE.CUSTOM) !== RULE.CUSTOM;
            return false;
        }
        if (code === RULE.OCG) return (cardRule & RULE.OCG) && !(cardRule & RULE.TCG);
        if (code === RULE.TCG) return (cardRule & RULE.TCG) && !(cardRule & RULE.OCG);
        return (cardRule & code) === code;
    };

    // 類型 Mask 判斷
    const hasType = (card, code) => {
        const type = Number(card?.type || 0);
        const target = Number(code || 0);
        if (type === 0 || target === 0) return false;
        return (type & target) > 0;
    };

    // 常用類型快速判斷介面
    const isType = {
        Monster: (card) => hasType(card, TYPE.MONSTER),
        Spell: (card) => hasType(card, TYPE.SPELL),
        Trap: (card) => hasType(card, TYPE.TRAP),
        Xyz: (card) => hasType(card, TYPE.XYZ),
        Link: (card) => hasType(card, TYPE.LINK),
        Pendulum: (card) => hasType(card, TYPE.PENDULUM),
        Extra: (card) => hasType(card, TYPE.FUSION | TYPE.SYNCHRO | TYPE.XYZ | TYPE.LINK),
    };

    // 字段判斷
    const isSetCard = (card, code) => {
        const cardSets = Array.isArray(card?.setcode) ? card.setcode : [];
        const target = Number(code || 0);
        const typeMain = target & 0x0FFF;
        const typeSub = target & 0xF000;
        return cardSets.some(cardCode => (cardCode & 0x0FFF) === typeMain && (cardCode & typeSub) === typeSub);
    };

    // 禁限狀態碼獲取
    const getCardLimitStatus = (card, iflistMap) => {
        if (!iflistMap || Object.keys(iflistMap).length === 0) return 3;
        const id = Number(card?.id || 0);
        for (const [statusCode, items] of Object.entries(iflistMap)) {
            if (Array.isArray(items) && items.some(item => Number(item.id) === id)) {
                const code = Number(statusCode);
                if (code >= 0 && code <= 2) return code;
            }
        }
        return 3;
    };

    // 複合狀態檢查
    const MatcheFilterCards = (cards, selectedFilter, iflistMap = {}) => {
        // 1. 統一對 selectedFilter 進行預先格式化與預設值補齊
        let ruleIndex = Number(selectedFilter.ruleIndex || 0);
        let ruleCode = Number(selectedFilter.ruleCode || 0);
        if (ruleIndex === 1) ruleCode = -1;
        const normalizedFilter = {
            nameId: (selectedFilter.nameId || '').trim().toLowerCase(),
            effect: (selectedFilter.effect || '').trim().toLowerCase(),
            linkMarker: Number(selectedFilter.linkMarker || 0),
            linkMode: Number(selectedFilter.linkMode || 0),
            category: BigInt(selectedFilter.category || 0n),
            ruleCode: ruleCode,
            iflistStatus: Number(selectedFilter.iflistStatus || 0),
            typMode: Number(selectedFilter.typMode || 0),
            typeMain: Number(selectedFilter.typeMain || 0),
            typeMonster: Number(selectedFilter.typeMonster || 0),
            typeSpellTrap: Number(selectedFilter.typeSpellTrap || 0),
            attribute: Number(selectedFilter.attribute || 0),
            race: Number(selectedFilter.race || 0),
            level: Number(selectedFilter.level || 0),
            scale: Number(selectedFilter.scale || 0),
            sets: Array.isArray(selectedFilter.sets) ? selectedFilter.sets : [],
            setMode: Number(selectedFilter.setMode || 0),
            atkMin: selectedFilter.atkMin === '?' ? -2 : (selectedFilter.atkMin === '' ? null : Number(selectedFilter.atkMin)),
            atkMax: selectedFilter.atkMax === '?' ? -2 : (selectedFilter.atkMax === '' ? null : Number(selectedFilter.atkMax)),
            defMin: selectedFilter.defMin === '?' ? -2 : (selectedFilter.defMin === '' ? null : Number(selectedFilter.defMin)),
            defMax: selectedFilter.defMax === '?' ? -2 : (selectedFilter.defMax === '' ? null : Number(selectedFilter.defMax))
        };

        // 2. 確保 iflistMap 一定是物件，避免後續重複判斷
        const normalizedIflist = (iflistMap && typeof iflistMap === 'object') ? iflistMap : {};

        // 3. 執行扁平化、過濾與排序
        return cards.flat().filter(card => cardMatchesFilter(card, normalizedFilter, normalizedIflist)).sort(sortCards);
    };

    const cardMatchesFilter = (card, selectedFilter, iflistMap) => {
        const cardType = Number(card.type || 0);

        let matchNameOrId = true;
        let matchEffect = true;
        let matchLinkMarker = true;
        let matchCategory = true;

        const selectedNameId = selectedFilter.nameId;
        if (selectedNameId) {
            matchNameOrId = (card.name || '').toLowerCase().includes(selectedNameId);
            const selectedId = Number(selectedNameId);
            if (!isNaN(selectedId)) {
                const cardId = String(card.id || '');
                matchNameOrId ||= cardId.startsWith(selectedNameId) || cardId.endsWith(selectedNameId);
                matchNameOrId ||= selectedId === card.alias;
            }
        }

        const selectedEffect = selectedFilter.effect;
        if (selectedEffect) {
            matchEffect = (card.desc || '').toLowerCase().includes(selectedEffect);
        }

        const selectedLinkMask = selectedFilter.linkMarker;
        if (selectedLinkMask) {
            if (selectedFilter.linkMode === 1) {
                matchLinkMarker = (selectedLinkMask & Number(card.linkMarker || 0)) === selectedLinkMask;
            } else {
                matchLinkMarker = matchBitmask(card.linkMarker, selectedLinkMask);
            }
        }

        const selectedCategory = selectedFilter.category;
        if (selectedCategory > 0n) {
            const cardCategory = BigInt(card.category || 0);
            matchCategory = (selectedCategory & cardCategory) > 0n;
        }

        return matchNameOrId && matchEffect && matchCategory && matchLinkMarker
            && isRule(card, selectedFilter.ruleCode)
            && matchIflist(card, selectedFilter.iflistStatus, iflistMap)
            && matchBitmask(card.type, selectedFilter.typeMain)
            && matchTypeMonster(cardType, selectedFilter.typeMonster, selectedFilter.typMode)
            && matchTypeSpellTrap(cardType, selectedFilter.typeSpellTrap)
            && matchBitmask(card.attribute, selectedFilter.attribute)
            && matchBitmask(card.race, selectedFilter.race)
            && matchLvScale(card.level, selectedFilter.level)
            && matchLvScale(card.scalePlus, selectedFilter.scale)
            && matchAtkDefPreprocessed(card, selectedFilter, 'atk')
            && matchAtkDefPreprocessed(card, selectedFilter, 'def')
            && matchSetcode(card, selectedFilter);
    };

    const matchIflist = (card, iflistStatus, iflistMap) => {
        if (iflistStatus === 0 || Object.keys(iflistMap).length === 0) return true;

        const cid = Number(card?.id);
        const targetCount = iflistStatus - 1; // 1->0(禁止), 2->1(限制), 3->2(準限制), 4->3(無限制)
        if (targetCount === 3) {
            for (const items of Object.values(iflistMap)) {
                if (Array.isArray(items) && items.some(item => Number(item.id) === cid)) {
                    return false;
                }
            }
            return true;
        }
        const items = iflistMap[targetCount] || iflistMap[String(targetCount)];
        return Array.isArray(items) && items.some(item => Number(item.id) === cid);
    };

    const matchTypeMonster = (cardType, selectedMask, typMode = 0) => {
        if (selectedMask === 0 || selectedMask === 1) return true;
        if ((cardType & TYPE.MONSTER) === 0) return false;
        if (typMode === 0) return (selectedMask & cardType) > 0;
        return (selectedMask & cardType) === selectedMask;
    };

    const matchTypeSpellTrap = (cardType, selectedMask) => {
        if (selectedMask === 0) return true;
        const withTypeSpellTrap = cardType & TYPE.ST;
        if (withTypeSpellTrap === 0) return false;
        if ((selectedMask & TYPE.NORMAL) > 0 && withTypeSpellTrap === cardType) return true;
        return (selectedMask & cardType) > 0;
    };

    const matchBitmask = (cardValue, filterValue) => filterValue === 0 || (filterValue & Number(cardValue || 0)) > 0;

    const matchLvScale = (cardValue, filterValue) => {
        if (!filterValue) return true;
        const cardVal = Number(cardValue || 0);
        return Boolean(cardVal && (filterValue & (1 << (cardVal - 1))));
    };

    const matchAtkDefPreprocessed = (card, selectedFilter, property) => {
        const min = selectedFilter[property + 'Min'];
        const max = selectedFilter[property + 'Max'];
        const cardVal = card[property];

        if (min === -2 || max === -2) return cardVal === -2;
        return (min === null || cardVal >= min) && (max === null || cardVal <= max);
    };

    const matchSetcode = (card, selectedFilter) => {
        const selectedSets = selectedFilter.sets;
        if (!selectedSets || selectedSets.length === 0) return true;

        if (selectedFilter.setMode === 1) {
            return selectedSets.every(targetCode => isSetCard(card, targetCode));
        } else {
            return selectedSets.some(targetCode => isSetCard(card, targetCode));
        }
    };

    const cardData = {
        sortCards,
        isRule,
        hasType,
        isType,
        isSetCard,
        getCardLimitStatus,
        MatcheFilterCards
    };

    window.APP_CONSTANTS = Object.assign(window.APP_CONSTANTS || {}, cardData);
    window.cardData = cardData;
})(window);