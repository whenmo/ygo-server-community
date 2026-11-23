function useBoardHelper() {
    /** 查詢所有公告 */
    const getAllBoards = async () => {
        const response = await axios.get('/api/board/all')
        return Array.isArray(response.data) ? [...response.data] : [];
    };

    /** 根據 boardId 查詢公告 */
    const getBoardById = async (boardId) => {
        const response = await axios.get(`/api/board/id/${boardId}`);
        return response.data;
    };

    /** 新增公告 */
    const createBoard = async (boardData) => {
        const response = await axios.post('/api/board', boardData);
        return response.data;
    };

    /** 更新公告 */
    const updateBoard = async (boardId, boardData) => {
        const response = await axios.put(`/api/board/id/${boardId}`, boardData);
        return response.data;
    };

    /** 刪除公告 */
    const deleteBoard = async (boardId) => {
        await axios.delete(`/api/board/id/${boardId}`);
    };

    return {
        getAllBoards,
        getBoardById,
        createBoard,
        updateBoard,
        deleteBoard
    };
}

window.useBoardHelper = useBoardHelper;