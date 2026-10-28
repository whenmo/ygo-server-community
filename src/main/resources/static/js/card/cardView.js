(function (window) {
    const { isType, RULE_CN, TYPE_CN, ATTRIBUTE_CN, RACE_CN, CATEGORY_CN } = window.APP_CONSTANTS || {};

    const getTypesView = (card) => getNamesByMask(card?.type, TYPE_CN).join(' | ');
    const getCategorysView = (card) => getNamesByMask(card?.category, CATEGORY_CN).join(' | ');
    const getAttributesView = (card) => getNamesByMask(card?.attribute, ATTRIBUTE_CN).join(' | ');
    const getRacesView = (card) => getNamesByMask(card?.race, RACE_CN).join(' | ');
    const getRulesView = (card) => getNamesByMask(card?.rule, RULE_CN).filter(name => name !== "O&T").join(' | ');
    const getLevelView = (card) => {
        const level = Number(card?.level || 0);
        if (isType.Link(card)) {
            return `LK-${level}`;
        } else if (isType.Xyz(card)) {
            return `RK-${level}`;
        }
        return `LV-${level}`;
    };

    const getPendulumView = (card) => {
        if (isType.Pendulum(card)) return '';
        return `P-${Number(card?.scalePlus || 0) - 1}`;
    };

    // 格式化攻防數值 (例如 -2 顯示為 '?')
    const formatAtkDefValue = (value) => (value === -2) ? '?' : Number(value).toLocaleString('zh-Hans');

    // 取得卡片 ATK/DEF 顯示字串
    const getCardAttributeRace = (card) => [getAttributesView(card), getRacesView(card)].filter(Boolean).join(' | ');

    // 取得卡片 ATK/DEF 顯示字串
    const getCardAtkDef = (card) => {
        return `${formatAtkDefValue(card?.atk)}${isType.Link(card) ? '' : `/${formatAtkDefValue(card?.def)}`}`;
    };

    // 根據遮罩與對照表轉換出對應的中文名稱清單
    const getNamesByMask = (maskValue, mappingObj) => {
        const mask = Number(maskValue || 0);
        if (!mask || !mappingObj) return [];
        return Object.entries(mappingObj).filter(([code]) => (mask & Number(code)) !== 0).map(([, name]) => name);
    };

    // 取得卡片 種族 屬性 lv 列
    const getCardInfoRowTwo = (card) => {
        if (!isType.Monster(card)) return getTypesView(card);
        return `${getCardAttributeRace(card)} ${getLevelView(card)}`;
    };

    // 取得卡片 atk/def scale 列
    const getCardInfoRowThree = (card) => {
        const scale = Number(card?.scalePlus || 0);
        const pendulum = (isType.Pendulum(card) && scale > 0) ? ` P-${scale - 1}` : '';
        return `${getCardAtkDef(card)}${pendulum}`;
    };

    // 格式化卡片效果文本並替換「」內的文字
    const getFormattedDesc = (card, configId, onClickCallback) => {
        if (!card?.desc) return '沒有效果文本。';

        const escaped = card.desc.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
        return escaped.replace(/「([^」]+)」/g, (match, p1) => {
            if (typeof onClickCallback === 'function') {
                const callbackName = `_handleDescClick_${Math.random().toString(36).substring(2)}`.replace('.', '');
                window[callbackName] = (e) => {
                    e.preventDefault();
                    onClickCallback(p1);
                };
                return `「<a class="effect-href" onclick="window['${callbackName}'](event); return false;">${p1}</a>」`;
            }

            const targetUrl = `/view/cards?cdb=${configId}&nameId=${encodeURIComponent(p1)}`;
            return `「<a class="effect-href" href="${targetUrl}">${p1}</a>」`;
        });
    };

    // 產生卡片圖片網址
    const FALLBACK_UNKNOWN_CARD = 'https://cdn.jsdelivr.net/gh/whenmo/ygo-server-community/src/main/resources/static/image/unknown_card.jpg';

    const getCardUrl = (card) => {
        const picCdn = typeof card?.picCdn === 'string' ? card.picCdn.trim() : '';
        if (!picCdn || !card?.id) return null;

        return `${picCdn}/${card.id}.jpg`;
    };

    const getCardLimitUrl = (card, iflistMap) => {
        const status = window.APP_CONSTANTS?.getCardLimitStatus?.(card, iflistMap);
        if (![0, 1, 2].includes(status)) return null;
        return `https://cdn.jsdelivr.net/gh/whenmo/ygo-server-community/src/main/resources/static/image/limit/limit_${status}.png`;
    };

    // 圖片載入失敗時的預設圖處置
    const getCardUrlError = (event) => {
        const image = event?.target;
        if (!image) return;

        if (!image.dataset.triedFallback) {
            image.dataset.triedFallback = 'true';
            image.src = FALLBACK_UNKNOWN_CARD;
        } else {
            image.onerror = null;
            image.removeAttribute('src');
            image.style.display = 'none';
        }
    };

    const cardView = {
        getTypesView,
        getCategorysView,
        getAttributesView,
        getRacesView,
        getRulesView,
        getPendulumView,
        getLevelView,
        getCardAttributeRace,
        formatAtkDefValue,
        getCardAtkDef,
        getCardUrl,
        getCardLimitUrl,
        getCardUrlError,
        getCardInfoRowTwo,
        getCardInfoRowThree,
        getFormattedDesc
    };

    window.APP_CONSTANTS = Object.assign(window.APP_CONSTANTS || {}, cardView);
    window.cardView = cardView;
})(window);
