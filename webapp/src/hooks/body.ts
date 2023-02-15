import { onMounted, onUnmounted } from "vue";

export function useBodyNoScrollbar() {
    onMounted(() => {
        document.body.classList.add("no-scrollbar");
    });

    onUnmounted(() => {
        document.body.classList.remove("no-scrollbar");
    });
}