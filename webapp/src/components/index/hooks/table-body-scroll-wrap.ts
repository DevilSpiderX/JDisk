import { Table as ATable } from "@arco-design/web-vue";
import { computed, Ref } from "vue";

export function useTableBodyScrollWrap(tableRef: Ref<InstanceType<typeof ATable> | null>) {
    const tableBodyScrollWrap = computed<HTMLDivElement | null>(() => tableRef.value ?
        tableRef.value.$el.querySelector(".arco-scrollbar-container.arco-table-body") : null);

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