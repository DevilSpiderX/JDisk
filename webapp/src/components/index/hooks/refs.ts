import { Scrollbar as AScrollbar } from "@arco-design/web-vue";
import { ref } from "vue";

export function useScrollbarRef() {
    return ref<InstanceType<typeof AScrollbar> | null>(null);
}

export function useTextWrapperRef() {
    return ref<HTMLDivElement | null>(null);
}