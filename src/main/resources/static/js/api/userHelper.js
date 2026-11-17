function useUserHelper() {
    /** 查詢所有用戶 */
    const loadUsers = async () => {
        const response = await axios.get('/api/users');
        return Array.isArray(response.data) ? response.data : [];
    };

    /** 根據 ID 查詢用戶 */
    const getUser = async (userId) => {
        const response = await axios.get(`/api/users/id/${userId}`);
        return response.data;
    };

    /** 註冊用戶 */
    const createUser = async (userData) => {
        const response = await axios.post('/api/users', userData);
        return response.data;
    };

    /** 用戶登入驗證 */
    const loginUser = async (credentials) => {
        const response = await axios.post('/api/users/login', credentials);
        return response.data;
    };

    /** 管理員更新用戶資訊 */
    const updateAdminUserInfo = async (userId, userData) => {
        const response = await axios.put(`/api/users/admin/info/${userId}`, userData);
        return response.data;
    };

    /** 管理員更新用戶密碼 */
    const updateAdminUserPassword = async (userId, passwordData) => {
        const response = await axios.put(`/api/users/admin/password/${userId}`, passwordData);
        return response.data;
    };

    /** 用戶更新自己的資訊 */
    const updateProfileInfo = async (userData) => {
        const response = await axios.put('/api/users/profile/info', userData);
        return response.data;
    };

    /** 用戶更新自己的密碼 */
    const updateProfilePassword = async (passwordData) => {
        const response = await axios.put('/api/users/profile/password', passwordData);
        return response.data;
    };

    /** 刪除用戶 */
    const deleteUser = async (userId) => {
        await axios.delete(`/api/users/id/${userId}`);
    };

    return {
        loadUsers,
        getUser,
        createUser,
        loginUser,
        updateAdminUserInfo,
        updateAdminUserPassword,
        updateProfileInfo,
        updateProfilePassword,
        deleteUser
    };
}

window.useUserHelper = useUserHelper;