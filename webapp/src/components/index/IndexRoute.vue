<script setup>
import { useAppConfigs } from "@/store/AppConfigsStore";
import { useRoute } from "vue-router";

const appConfigs = useAppConfigs();

const route = useRoute();

const props = defineProps({
    driverKey: {
        type: String
    },
    path: {
        type: Array,
        default: []
    }
});

</script>

<template>
    <div>

    </div>
</template>

<style scoped></style>