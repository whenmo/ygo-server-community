function useDiyerHelper() {
    /** 查詢所有作者 */
    const loadDiyers = async () => {
        const response = await axios.get('/api/diyers');
        return Array.isArray(response.data) ? [...response.data] : [];
    };

    /** 根據 ID 查詢作者 */
    const getDiyer = async (diyerId) => {
        const response = await axios.get(`/api/diyers/id/${diyerId}`);
        return response.data;
    };

    /** 根據 cdbId 查詢作者列表 */
    const loadDiyersByCdbId = async (cdbId) => {
        const response = await axios.get(`/api/diyers/cdb/${cdbId}`);
        return Array.isArray(response.data) ? [...response.data] : [];
    };

    /** 根據 userId 查詢作者列表 */
    const loadDiyersByUserId = async (userId) => {
        const response = await axios.get(`/api/diyers/user/${userId}`);
        return Array.isArray(response.data) ? [...response.data] : [];
    };

    /** 新增作者 */
    const createDiyer = async (diyerData) => {
        const response = await axios.post('/api/diyers', diyerData);
        return response.data;
    };

    /** 更新作者 */
    const updateDiyer = async (diyerId, diyerData) => {
        const response = await axios.put(`/api/diyers/id/${diyerId}`, diyerData);
        return response.data;
    };

    /** 刪除作者 */
    const deleteDiyer = async (diyerId) => {
        await axios.delete(`/api/diyers/id/${diyerId}`);
    };

    return {
        loadDiyers,
        getDiyer,
        loadDiyersByCdbId,
        loadDiyersByUserId,
        createDiyer,
        updateDiyer,
        deleteDiyer
    };
}

window.useDiyerHelper = useDiyerHelper;