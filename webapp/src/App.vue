<script setup>
import { http } from "@/scripts/http";
import { useRouter } from "vue-router";
import { useAppConfigs } from "./store/AppConfigsStore";
import { useSystemConfigs } from "./store/SystemConfigs";

const appConfigs = useAppConfigs();

window.matchMedia("(prefers-color-scheme:dark)").onchange = event => {
    if (appConfigs.themeFollowSystem) {
        appConfigs.darkTheme = event.matches;
    }
};

const systemConfigs = useSystemConfigs(), router = useRouter();

http.systemConfig.list().then(resp => {
    if (resp.code === 0) {
        Object.assign(systemConfigs.values, resp.data);
        if (systemConfigs.values.installed === false) {
            router.addRoute({
                name: "install",
                path: "/install",
                component: () => import("@/components/install/InstallRoute.vue"),
                meta: { title: "JDisk|初始化" },
            });
            router.push({ name: "install" });
        }
    }
});

</script>

<template>
    <RouterView />
</template>
