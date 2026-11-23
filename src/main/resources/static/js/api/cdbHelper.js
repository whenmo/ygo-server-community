function useCdbHelper() {
    // ============================================================
    // Card 相關
    // ============================================================
    let allCards = null;
    let cardLoadingPromise = null;

    /** 載入所有 CDB 的組合卡片資料 */
    const loadAllCards = async () => {
        if (allCards) return allCards;
        if (cardLoadingPromise) return cardLoadingPromise;

        cardLoadingPromise = await axios.get('/api/cdb/card/all')
            .then(response => {
                allCards = Array.isArray(response.data) ? [...response.data] : [];
                return allCards;
            })
            .finally(() => {
                cardLoadingPromise = null;
            });

        return cardLoadingPromise;
    };

    /** 根據 cdbId 與 cardId 查詢單張卡片 */
    const findCard = async (cdbId, cardId) => {
        const response = await axios.get(`/api/cdb/id/${cdbId}/card/${cardId}`);
        return response.data || null;
    };

    /** 根據 cardId 查詢單張卡片 */
    const findCardById = async (cardId) => {
        const response = await axios.get(`/api/cdb/card/${cardId}`);
        return response.data || null;
    };

    /** 載入指定 CDB 的所有卡片 */
    const loadCardsByCdb = async (cdbId) => {
        const response = await axios.get(`/api/cdb/id/${cdbId}/card`);
        return Array.isArray(response.data) ? [...response.data] : [];
    };

    /** 刷新指定 cdbId 的卡片快取 */
    const refreshCard = async (cdbId) => {
        await axios.post(`/api/cdb/id/${cdbId}/card/refresh`);
        allCards = null;
    };

    // ============================================================
    // Lflist 相關（自行內部管理狀態）
    // ============================================================
    const LFLIST_VER_NA = '__NA__';
    const lflistsMap = Vue.ref({});
    let lflistAllLoaded = false;
    let lflistLoadingPromise = null;

    /** 載入所有禁限卡表資料並依 cdbId 分組 */
    const loadAllLflists = async () => {
        if (lflistAllLoaded) return Object.values(lflistsMap.value).flat();
        if (lflistLoadingPromise) return lflistLoadingPromise;

        lflistLoadingPromise = axios.get('/api/cdb/lflist/all')
            .then(response => {
                const grouped = {};
                const lflists = Array.isArray(response.data) ? [...response.data] : [];
                lflists.forEach(item => {
                    const key = String(item.cdbId);
                    if (!grouped[key]) grouped[key] = [];
                    grouped[key].push(item);
                });
                lflistsMap.value = grouped;
                lflistAllLoaded = true;
                return lflists;
            })
            .catch(() => {
                lflistsMap.value = {};
                return [];
            })
            .finally(() => {
                lflistLoadingPromise = null;
            });

        return lflistLoadingPromise;
    };

    /** 刷新指定 cdbId 的禁限卡表快取 */
    const refreshLflist = async (cdbId) => {
        await axios.post(`/api/cdb/id/${cdbId}/lflist/refresh`);
        lflistAllLoaded = false;
    };

    /** 載入指定 CDB 的禁限卡表 */
    const loadLflistsByCdb = async (cdbId) => {
        const response = await axios.get(`/api/cdb/id/${cdbId}/lflist`);
        return Array.isArray(response.data) ? [...response.data] : [];
    };

    // ============================================================
    // Strings 相關
    // ============================================================
    let allStrings = null;
    const stringsByConfig = new Map();
    let defaultSetnames = null;
    let defaultSetnamesPromise = null;
    let defaultStrings = null;
    let defaultStringsPromise = null;
    let stringsLoadingPromise = null;
    const configStringsLoadingPromises = new Map();

    /** 載入所有 strings.conf 的組合檔案 */
    const loadAllStrings = async () => {
        if (allStrings) return allStrings;
        if (stringsLoadingPromise) return stringsLoadingPromise;

        stringsLoadingPromise = axios.get('/api/cdb/strings/all')
            .then(response => {
                allStrings = response.data || { setnames: [], counters: [], victorys: [] };
                return allStrings;
            })
            .finally(() => {
                stringsLoadingPromise = null;
            });

        return stringsLoadingPromise;
    };

    /** 根據 cdbId 載入 strings.conf 的檔案 */
    const loadStringsByConfig = async (configId) => {
        const key = String(configId);
        if (stringsByConfig.has(key)) return stringsByConfig.get(key);
        if (configStringsLoadingPromises.has(key)) return configStringsLoadingPromises.get(key);

        const request = axios.get(`/api/cdb/id/${encodeURIComponent(configId)}/strings`)
            .then(response => {
                const strings = response.data || { setnames: [], counters: [], victorys: [] };
                stringsByConfig.set(key, strings);
                return strings;
            })
            .finally(() => configStringsLoadingPromises.delete(key));

        configStringsLoadingPromises.set(key, request);
        return request;
    };

    /** 載入預設 strings.conf 的字段 */
    const loadDefaultSetnames = async () => {
        if (defaultSetnames) return defaultSetnames;
        if (defaultSetnamesPromise) return defaultSetnamesPromise;

        defaultSetnamesPromise = axios.get('/api/cdb/strings/default')
            .then(response => {
                defaultSetnames = Array.isArray(response.data?.setnames) ? [...response.data.setnames] : [];
                return defaultSetnames;
            })
            .finally(() => {
                defaultSetnamesPromise = null;
            });

        return defaultSetnamesPromise;
    };

    /** 載入預設 strings.conf 的檔案 */
    const loadDefaultStrings = async () => {
        if (defaultStrings) return defaultStrings;
        if (defaultStringsPromise) return defaultStringsPromise;

        defaultStringsPromise = axios.get('/api/cdb/strings/default')
            .then(response => {
                defaultStrings = response.data || { setnames: [], counters: [], victorys: [] };
                return defaultStrings;
            })
            .finally(() => {
                defaultStringsPromise = null;
            });

        return defaultStringsPromise;
    };

    /** 刷新指定 cdbId 的 strings.conf 的檔案快取 */
    const refreshStrings = async (cdbId) => {
        await axios.post(`/api/cdb/id/${cdbId}/strings/refresh`);
        stringsByConfig.delete(String(cdbId));
        allStrings = null;
    };

    /** 重新從遠端下載並刷新預設的 strings.conf 快取 */
    const refreshDefaultStrings = async () => {
        await axios.post('/api/cdb/strings/default/refresh');
        defaultStrings = null;
        defaultSetnames = null;
        defaultStringsPromise = null;
        defaultSetnamesPromise = null;
    };

    return {
        // Card
        loadAllCards,
        loadCardsByCdb,
        findCard,
        findCardById,
        refreshCard,
        // Lflist
        LFLIST_VER_NA,
        loadAllLflists,
        loadLflistsByCdb,
        refreshLflist,
        // Strings
        loadAllStrings,
        loadStringsByConfig,
        loadDefaultSetnames,
        loadDefaultStrings,
        refreshStrings,
        refreshDefaultStrings
    };
}

window.useCdbHelper = useCdbHelper;