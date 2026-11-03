function useStringsHelper() {
    let allStrings = null;
    const stringsByConfig = new Map();
    let defaultSetnames = null;
    let defaultSetnamesPromise = null;
    let defaultStrings = null;
    let defaultStringsPromise = null;
    let loadingPromise = null;
    const configLoadingPromises = new Map();

    const loadAllStrings = async () => {
        if (allStrings) return allStrings;
        if (loadingPromise) return loadingPromise;

        loadingPromise = axios.get('/api/cdb/strings/all')
            .then(response => {
                allStrings = response.data || { setnames: [], counters: [], victorys: [] };
                return allStrings;
            })
            .finally(() => {
                loadingPromise = null;
            });

        return loadingPromise;
    };

    const loadStringsByConfig = async (configId) => {
        const key = String(configId);
        if (stringsByConfig.has(key)) return stringsByConfig.get(key);
        if (configLoadingPromises.has(key)) return configLoadingPromises.get(key);

        const request = axios.get(`/api/cdb/strings/config/${encodeURIComponent(configId)}`)
            .then(response => {
                const strings = response.data || { setnames: [], counters: [], victorys: [] };
                stringsByConfig.set(key, strings);
                return strings;
            })
            .finally(() => configLoadingPromises.delete(key));

        configLoadingPromises.set(key, request);
        return request;
    };

    const loadDefaultSetnames = async () => {
        if (defaultSetnames) return defaultSetnames;
        if (defaultSetnamesPromise) return defaultSetnamesPromise;

        defaultSetnamesPromise = axios.get('/api/cdb/strings/default')
            .then(response => {
                defaultSetnames = Array.isArray(response.data?.setnames) ? response.data.setnames : [];
                return defaultSetnames;
            })
            .finally(() => {
                defaultSetnamesPromise = null;
            });

        return defaultSetnamesPromise;
    };

    const loadDefaultStrings = async () => {
        if (defaultStrings) return defaultStrings;
        if (defaultStringsPromise) return defaultStringsPromise;

        defaultStringsPromise = axios.get('/api/cdb/strings/default')
            .then(response => {
                defaultStrings = response.data || { setnames: [], counters: [], victorys: [] };
                return defaultStrings;
            })
            .finally(() => {
                defaultStringsPromise = null;
            });

        return defaultStringsPromise;
    };

    return { loadAllStrings, loadStringsByConfig, loadDefaultSetnames, loadDefaultStrings };
}

window.useStringsHelper = useStringsHelper;
