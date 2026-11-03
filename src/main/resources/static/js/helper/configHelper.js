function useConfigHelper() {
    let configs = null;
    let loadingPromise = null;

    const loadAllConfigs = async () => {
        if (configs) return configs;
        if (loadingPromise) return loadingPromise;

        loadingPromise = axios.get('/api/cdb_config')
            .then(response => {
                configs = Array.isArray(response.data) ? response.data : [];
                return configs;
            })
            .finally(() => {
                loadingPromise = null;
            });

        return loadingPromise;
    };

    return { loadAllConfigs };
}

window.useConfigHelper = useConfigHelper;
