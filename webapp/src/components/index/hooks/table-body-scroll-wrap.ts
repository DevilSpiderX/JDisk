import { onMounted, ref } from "vue";

export function useTableBodyScrollWrap(tableSelector: string) {
    const tableBodyScrollWrap = ref<HTMLDivElement | null>(null);

    onMounted(() => {
        tableBodyScrollWrap.value = document.querySelector(tableSelector + ' .arco-scrollbar-container.arco-table-body');
    });

    function setTableScrollLeft(num: number) {
        if (tableBodyScrollWrap.value)
            tableBodyScrollWrap.value.scrollLeft = num;
    }

    function setTableScrollTop(num: number) {
        if (tableBodyScrollWrap.value)
            tableBodyScrollWrap.value.scrollTop = num;
    }

    return {
        tableBodyScrollWrap,
        setTableScrollLeft,
        setTableScrollTop
    }
}