function useDeckHelper() {
    let publicDecks = null;
    let mineDecks = null;
    let publicDecksPromise = null;
    let mineDecksPromise = null;

    /** 查詢所有公開卡組 */
    const loadPublicDecks = async (forceRefresh = false) => {
        if (!forceRefresh && publicDecks) return publicDecks;
        if (publicDecksPromise) return publicDecksPromise;

        publicDecksPromise = axios.get('/api/decks/public')
            .then(response => {
                publicDecks = Array.isArray(response.data) ? [...response.data] : [];
                return publicDecks;
            }).finally(() => {
                publicDecksPromise = null;
            });

        return publicDecksPromise;
    };

    /** 查詢登入用戶的所有卡組 */
    const loadMineDecks = async (forceRefresh = false) => {
        if (!forceRefresh && mineDecks) return mineDecks;
        if (mineDecksPromise) return mineDecksPromise;

        mineDecksPromise = axios.get('/api/decks/mine')
            .then(response => {
                mineDecks = Array.isArray(response.data) ? [...response.data] : [];
                return mineDecks;
            }).finally(() => {
                mineDecksPromise = null;
            });

        return mineDecksPromise;
    };

    /** 查詢指定公開卡組 */
    const getPublicDeck = async (deckId) => {
        const response = await axios.get(`/api/decks/public/${deckId}`);
        return response.data;
    };

    /** 查詢登入用戶指定卡組 */
    const getMineDeck = async (deckId) => {
        const response = await axios.get(`/api/decks/mine/${deckId}`);
        return response.data;
    };

    /** 下載指定公開卡組 */
    const downloadPublicDeck = async (deckId) => {
        const response = await axios.get(`/api/decks/public/${deckId}/download`, { responseType: 'blob' });
        return response.data;
    };

    /** 下載登入用戶的指定卡組 */
    const downloadMineDeck = async (deckId) => {
        const response = await axios.get(`/api/decks/mine/${deckId}/download`, { responseType: 'blob' });
        return response.data;
    };

    /** 建立卡組 */
    const createDeck = async (deckData) => {
        const response = await axios.post('/api/decks', deckData);
        publicDecks = null;
        mineDecks = null;
        return response.data;
    };

    /** 更新卡組 */
    const updateDeck = async (deckId, deckData) => {
        const response = await axios.put(`/api/decks/${deckId}`, deckData);
        publicDecks = null;
        mineDecks = null;
        return response.data;
    };

    /** 刪除卡組 */
    const deleteDeck = async (deckId) => {
        await axios.delete(`/api/decks/${deckId}`);
        publicDecks = null;
        mineDecks = null;
    };

    /**
     * 讀取並解析傳入的 .ydk 檔案
     * @param {File} file YDK 檔案物件
     * @returns {Promise<Object>} 包含 main, extra, side, pack 的物件
     */
    const loadYdkFile = async (file) => {
        const content = await file.text();
        const lines = content.split(/\r?\n/);
        let currentSection = 'pack';
        const main = [];
        const extra = [];
        const side = [];
        const pack = [];

        for (let line of lines) {
            line = line.trim();
            if (!line) continue;

            if (line.startsWith('#main')) {
                currentSection = 'main';
                continue;
            } else if (line.startsWith('#extra')) {
                currentSection = 'extra';
                continue;
            } else if (line.startsWith('!side') || line.startsWith('#side')) {
                currentSection = 'side';
                continue;
            } else if (line.startsWith('#')) {
                continue;
            }

            const cardId = Number(line);
            if (isNaN(cardId) || cardId <= 0) continue;

            if (currentSection === 'pack') {
                pack.push(cardId);
            } else if (currentSection === 'main') {
                main.push(cardId);
            } else if (currentSection === 'extra') {
                extra.push(cardId);
            } else if (currentSection === 'side') {
                side.push(cardId);
            }
        }
        return { main, extra, side, pack };
    };

    return {
        loadPublicDecks,
        loadMineDecks,
        getPublicDeck,
        getMineDeck,
        downloadPublicDeck,
        downloadMineDeck,
        createDeck,
        updateDeck,
        deleteDeck,
        loadYdkFile
    };
}

window.useDeckHelper = useDeckHelper;