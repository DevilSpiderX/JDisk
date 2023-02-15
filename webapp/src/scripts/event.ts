import { onMounted, onUnmounted } from 'vue';

export function useEventListener(target: EventTarget | string, event: string,
    callback: EventListenerOrEventListenerObject, options?: boolean | AddEventListenerOptions) {
    let element: EventTarget | null;
    if (typeof target === "string") {
        element = document.querySelector(target);
    } else {
        element = target;
    }
    if (element) {
        onMounted(() => element?.addEventListener(event, callback, options));
        onUnmounted(() => element?.removeEventListener(event, callback, options));
    } else {
        throw new Error("target not found");
    }
}
