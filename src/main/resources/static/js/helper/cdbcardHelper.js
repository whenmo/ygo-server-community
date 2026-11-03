function useCdbCardHelper() {
    let allCards = null;
    let loadingPromise = null;

    const loadAllCards = async () => {
        if (allCards) return allCards;
        if (loadingPromise) return loadingPromise;

        loadingPromise = axios.get('/api/cdb/card/all')
            .then(response => {
                allCards = Array.isArray(response.data) ? response.data : [];
                return allCards;
            })
            .finally(() => {
                loadingPromise = null;
            });

        return loadingPromise;
    };

    const findCard = async (configId, cardId) => {
        const response = await axios.get(`/api/cdb/card/config/${configId}/${cardId}`);
        return response.data || null;
    };

    return { loadAllCards, findCard };
}

window.useCdbCardHelper = useCdbCardHelper;
