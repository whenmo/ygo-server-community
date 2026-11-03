function useLflistHelper(lflistsMapRef, selectVerRef, currentLflistRef) {
    const LFLIST_VER_NA = '__NA__';
    let allLoaded = false;
    let loadingPromise = null;

    const loadAllLflists = async () => {
        if (allLoaded) return Object.values(lflistsMapRef.value).flat();
        if (loadingPromise) return loadingPromise;

        loadingPromise = axios.get('/api/cdb/lflist/all')
            .then(response => {
                const grouped = {};
                const lflists = Array.isArray(response.data) ? response.data : [];
                lflists.forEach(item => {
                    const key = String(item.cdbId);
                    if (!grouped[key]) grouped[key] = [];
                    grouped[key].push(item);
                });
                lflistsMapRef.value = grouped;
                allLoaded = true;
                return lflists;
            })
            .catch(() => {
                lflistsMapRef.value = {};
                return [];
            })
            .finally(() => {
                loadingPromise = null;
            });

        return loadingPromise;
    };

    const loadCurrentLflist = async (configId, versionId) => {
        await loadAllLflists();
        const lflists = lflistsMapRef.value[String(configId)] || [];

        if (!configId || versionId === LFLIST_VER_NA || lflists.length === 0) {
            selectVerRef.value = LFLIST_VER_NA;
            currentLflistRef.value = {};
            return null;
        }

        const selected = versionId && lflists.find(item => item.version === versionId);
        const current = selected || lflists[0];
        selectVerRef.value = current.version;
        currentLflistRef.value = current;
        return current;
    };

    return {
        LFLIST_VER_NA,
        loadAllLflists,
        loadCurrentLflist
    };
}

window.useLflistHelper = useLflistHelper;
