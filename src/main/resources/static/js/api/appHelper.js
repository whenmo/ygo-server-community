function useAppHelper() {
    let applicationConfig = null;
    let loadingPromise = null;

    /** 載入應用程式設定 */
    const loadApplicationConfig = async (forceRefresh = false) => {
        if (!forceRefresh && applicationConfig) return applicationConfig;
        if (loadingPromise) return loadingPromise;

        loadingPromise = axios.get('/api/config')
            .then(response => {
                applicationConfig = response.data || {};
                return applicationConfig;
            })
            .finally(() => {
                loadingPromise = null;
            });

        return loadingPromise;
    };

    return { loadApplicationConfig };
}

window.useAppHelper = useAppHelper;