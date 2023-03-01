<script setup>
import { useAppConfigs } from "@/store/AppConfigsStore";
import { computed, ref } from "vue";
import Vue3VideoPlay from "vue3-video-play";
import "vue3-video-play/dist/style.css";

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

const width = computed(() => {
    const winWidth = appConfigs.client.width;
    if (winWidth < 576) {
        //xs [0, 576)
        return "90%";
    } else if (winWidth < 768) {
        //sm [576, 768)
        return "80%";
    } else if (winWidth < 992) {
        //md [768, 992)
        return "75%";
    } else if (winWidth < 1200) {
        //lg [992, 1200)
        return "70%";
    } else {
        //xl & xxl [1200, ∞)
        return "800px";
    }
});

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