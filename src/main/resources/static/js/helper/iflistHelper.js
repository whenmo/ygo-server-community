function useIflistHelper(iflistVersRef, currentIflistMapRef) {
    // 載入指定 CDB 的禁卡表版本清單
    const getIflistVersions = async (configId) => {
        if (!configId) {
            iflistVersRef.value = [];
            return;
        }
        try {
            const response = await axios.get(`/api/cdb/iflist/${configId}/versions`);
            const verList = Array.isArray(response.data) ? response.data : Array.from(response.data || []);
            iflistVersRef.value = verList;
        } catch (error) {
            iflistVersRef.value = [];
        }
    };

    // 載入指定 CDB 與版本的禁卡表內容
    const getIflistByVersion = async (configId, versionId) => {
        if (!configId || !versionId) {
            currentIflistMapRef.value = {};
            return;
        }
        try {
            const response = await axios.get(`/api/cdb/iflist/${configId}/version/${encodeURIComponent(versionId)}`);
            currentIflistMapRef.value = response.data || {};
        } catch (error) {
            currentIflistMapRef.value = {};
        }
    };

    return {
        getIflistVersions,
        getIflistByVersion
    };
}

window.useIflistHelper = useIflistHelper;