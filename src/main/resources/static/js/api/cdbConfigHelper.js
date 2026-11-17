function useCdbConfigHelper() {
    const CDB_VER_NA = '__ALL__';
    let cdbConfigs = null;
    let loadingPromise = null;

    /** 查詢所有 CDB 設定 */
    const loadAllConfigs = async (forceRefresh = false) => {
        if (!forceRefresh && cdbConfigs) return cdbConfigs;
        if (loadingPromise) return loadingPromise;

        loadingPromise = axios.get('/api/cdb-config/all')
            .then(response => {
                cdbConfigs = Array.isArray(response.data) ? response.data : [];
                return cdbConfigs;
            })
            .finally(() => {
                loadingPromise = null;
            });

        return loadingPromise;
    };

    /** 根據 cdbId 查詢單一 CDB 設定 */
    const getConfigById = async (cdbId) => {
        if (cdbConfigs) {
            const found = cdbConfigs.find(config => config.id === cdbId);
            if (found) return found;
        }

        const response = await axios.get(`/api/cdb-config/id/${cdbId}`);
        return response.data;
    };

    /** 新增 CDB 設定 */
    const createConfig = async (configData) => {
        const response = await axios.post('/api/cdb-config', configData);
        cdbConfigs = null;
        return response.data;
    };

    /** 更新 CDB 設定 */
    const updateConfig = async (cdbId, configData) => {
        const response = await axios.put(`/api/cdb-config/id/${cdbId}`, configData);
        cdbConfigs = null;
        return response.data;
    };

    /** 刪除 CDB 設定 */
    const deleteConfig = async (id) => {
        await axios.delete(`/api/cdb-config/id/${id}`);
        cdbConfigs = null;
    };

    return {
        CDB_VER_NA,
        loadAllConfigs,
        getConfigById,
        createConfig,
        updateConfig,
        deleteConfig
    };
}

window.useCdbConfigHelper = useCdbConfigHelper;