import { ref } from "vue";
import { LayoutHeader as ALayoutHeader } from "@arco-design/web-vue";

export function useHeaderRef() {
    return ref<InstanceType<typeof ALayoutHeader> | null>(null);
}