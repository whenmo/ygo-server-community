function useDeckPackHelper() {
    let publicDeckPacks = null;

    /** 查詢所有公開卡包 */
    let publicDeckPacksPromise = null;
    const loadPublicDeckPacks = async (forceRefresh = false) => {
        if (!forceRefresh && publicDeckPacks) return publicDeckPacks;
        if (publicDeckPacksPromise) return publicDeckPacksPromise;

        publicDeckPacksPromise = axios.get('/api/deck-packs/public')
            .then(response => {
                publicDeckPacks = Array.isArray(response.data) ? [...response.data] : [];
                return publicDeckPacks;
            }).finally(() => {
                publicDeckPacksPromise = null;
            });
        return publicDeckPacksPromise;
    };

    /** 查詢指定用戶的卡包 */
    const loadUserDeckPacks = async (userId) => {
        const response = await axios.get(`/api/deck-packs/private/${userId}`);
        return Array.isArray(response.data) ? [...response.data] : [];
    };

    /** 建立公開卡包 */
    const createPublicDeckPack = async (packData) => {
        const response = await axios.post('/api/deck-packs/public', packData);
        publicDeckPacks = null;
        return response.data;
    };

    /** 建立私人卡包 */
    const createPrivateDeckPack = async (packData) => {
        const response = await axios.post('/api/deck-packs/private', packData);
        return response.data;
    };

    /** 更新公開卡包 */
    const updatePublicDeckPack = async (packId, packData) => {
        const response = await axios.put(`/api/deck-packs/public/${packId}`, packData);
        publicDeckPacks = null;
        return response.data;
    };

    /** 更新私人卡包 */
    const updatePrivateDeckPack = async (packId, packData) => {
        const response = await axios.put(`/api/deck-packs/private/${packId}`, packData);
        return response.data;
    };

    /** 刪除公開卡包 */
    const deletePublicDeckPack = async (packId) => {
        await axios.delete(`/api/deck-packs/public/${packId}`);
        publicDeckPacks = null;
    };

    /** 刪除私人卡包 */
    const deletePrivateDeckPack = async (packId) => {
        await axios.delete(`/api/deck-packs/private/${packId}`);
    };

    return {
        loadPublicDeckPacks,
        loadUserDeckPacks,
        createPublicDeckPack,
        createPrivateDeckPack,
        updatePublicDeckPack,
        updatePrivateDeckPack,
        deletePublicDeckPack,
        deletePrivateDeckPack
    };
}

window.useDeckPackHelper = useDeckPackHelper;