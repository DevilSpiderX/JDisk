<script setup>
import { useAppConfigs } from "@/store/AppConfigsStore";
import { computed, ref } from "vue";
import Vue3VideoPlay from "vue3-video-play";
import "vue3-video-play/dist/style.css";
import { useModalWidth } from "../hooks/modal-width";

const appConfigs = useAppConfigs();

const props = defineProps({
    title: {
        type: String,
        default: "视频预览"
    },
    visible: {
        type: Boolean,
        default: false
    },
    videoSrc: {
        type: String
    }
});

const emit = defineEmits(["update:visible", "close"]);

const playerRef = ref();

const _visible = computed({
    get: () => {
        playerRef.value?.pause();
        return props.visible;
    },
    set: newVisible => emit("update:visible", newVisible)
});

function on_close() {
    emit("close");
}

const { width } = useModalWidth();

</script>

<template>
    <AModal v-model:visible="_visible" :width="width" :footer="false" draggable @close="on_close"
        :body-style="{ padding: '5px' }">
        <template #title>
            <div class="video-title">
                {{ title }}
            </div>
        </template>
        <Vue3VideoPlay width="100%" :src="videoSrc" :volume="1" ref="playerRef" preload="meta" />
    </AModal>
</template>

<style scoped>
.video-title {
    max-width: 270px;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
}
</style>