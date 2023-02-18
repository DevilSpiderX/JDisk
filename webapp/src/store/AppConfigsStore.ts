import { defineStore } from 'pinia';
import { computed, ref, watch, watchEffect } from 'vue';

const themeColorMetaElement = document.createElement("meta");
themeColorMetaElement.setAttribute("name", "theme-color");
document.head.append(themeColorMetaElement);

export const useAppConfigs = defineStore("appConfigs", () => {
    //setup
    const client = ref<{ width: number, height: number }>({
        width: document.documentElement.clientWidth,
        height: document.documentElement.clientHeight
    });

    new ResizeObserver(() => {
        const rect = document.documentElement.getBoundingClientRect();
        client.value.width = rect.width;
        client.value.height = rect.height;
    }).observe(document.documentElement);

    const darkTheme = ref<boolean>(false);

    const themeName = computed<string>(() => darkTheme.value ? "dark" : "light");

    watchEffect(() => {
        document.body.setAttribute("arco-theme", themeName.value);
    });

    const themeFollowSystem = ref<boolean>(false);

    watchEffect(() => {
        if (themeFollowSystem.value) {
            darkTheme.value = window.matchMedia('(prefers-color-scheme:dark)').matches;
        }
    });

    const statusBarColor = ref<string>("");

    watch(statusBarColor, statusBarColor => {
        themeColorMetaElement.setAttribute("content", statusBarColor);
    });

    watchEffect(() => {
        statusBarColor.value = darkTheme.value ? "#17171a" : "#ffffff";
    });

    return {
        client,
        darkTheme,
        themeName,
        themeFollowSystem,
        statusBarColor,
        isTouchDevice: ref<boolean>("ontouchstart" in window && navigator.maxTouchPoints !== 0)
    }
}, {
    persist: {
        storage: localStorage,
        paths: [
            "darkTheme",
            "themeFollowSystem",
            "isTouchDevice"
        ]
    }
});