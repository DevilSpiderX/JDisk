<script setup>
import { ref } from "vue";
import { http } from "./scripts/http";
import { useAppConfigs } from "./store/AppConfigsStore";

window.appInited = ref(false);
const appConfigs = useAppConfigs();

window.matchMedia("(prefers-color-scheme:dark)").onchange = event => {
    if (appConfigs.themeFollowSystem) {
        appConfigs.darkTheme = event.matches;
    }
};

</script>

<template>
    <RouterView />
</template>
