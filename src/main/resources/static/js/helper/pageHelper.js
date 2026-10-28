function usePageHelper(itemsRef, defaultPageSize = 20) {
    const { ref, computed } = Vue;
    const pageSizeOptions = [10, 20, 30, 40, 50];
    const pageSize = ref(defaultPageSize);
    const currentPage = ref(1);

    const totalPages = computed(() => Math.max(1, Math.ceil((itemsRef.value?.length || 0) / pageSize.value)));

    const pagedCards = computed(() => {
        const start = (currentPage.value - 1) * pageSize.value;
        return (itemsRef.value || []).slice(start, start + pageSize.value);
    });

    const previousPage = () => { if (currentPage.value > 1) currentPage.value--; };
    const nextPage = () => { if (currentPage.value < totalPages.value) currentPage.value++; };

    const onPageInput = (event) => {
        let value = event.target.value.replace(/[^0-9]/g, '');
        if (value === '') {
            currentPage.value = '';
            return;
        }
        let page = Number(value);
        if (page > totalPages.value) page = totalPages.value;
        else if (page < 1) page = 1;
        event.target.value = page;
        currentPage.value = page;
    };

    const validatePage = () => {
        let page = Number(currentPage.value);
        if (isNaN(page) || page < 1) currentPage.value = 1;
        else if (page > totalPages.value) currentPage.value = totalPages.value;
    };

    return {
        pageSizeOptions,
        pageSize,
        currentPage,
        totalPages,
        pagedCards,
        previousPage,
        nextPage,
        onPageInput,
        validatePage
    };
}

window.usePageHelper = usePageHelper;