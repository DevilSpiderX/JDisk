<script setup>
import { Scrollbar as AScrollbar } from "@arco-design/web-vue";
import { computed, reactive } from 'vue';
import { useTextWrapperRef, useScrollbarRef } from "../hooks/refs";

const props = defineProps({
    text: {
        type: String,
        default: "",
        request: true
    },
    fontSize: {
        type: Number,
        default: 16,
        request: false
    },
    loading: {
        type: Boolean,
        default: false,
        request: false
    }
});

const textLines = computed(() => {
    return props.text.split(/\n|\r\n/);
});

const scrollbarRef = useScrollbarRef();

function backTop(isSmooth) {
    scrollbarRef.value?.scrollTo({ top: 0, behavior: isSmooth ? "smooth" : undefined });
}

const scrollData = reactive({
    left: 0,
    top: 0
});

function on_scrollbar_scroll(event) {
    scrollData.left = event.target?.scrollLeft;
    scrollData.top = event.target?.scrollTop;
}

const textWrapperRef = useTextWrapperRef();
const textWrapperStyle = computed(() => ({
    fontSize: props.fontSize + "px"
}));

function toBottom(isSmooth) {
    scrollbarRef.value?.scrollTo({ top: logWrapperRef.value?.clientHeight, behavior: isSmooth ? "smooth" : undefined });
}

defineExpose({
    backTop,
    toBottom
});
</script>

<template>
    <ASpin :loading="loading" class="text-monitor-spin">
        <AScrollbar class="text-monitor-scrollbar" outer-class="text-monitor-scrollbar-outer" @scroll="on_scrollbar_scroll"
            ref="scrollbarRef">
            <div class="text-wrapper" :style="textWrapperStyle" ref="textWrapperRef">
                <div v-for="(item, index) of textLines" class="text-line">
                    <span class="serial"> {{ index + 1 }} </span>
                    <div class="text-content"> {{ item }} </div>
                </div>
            </div>
        </AScrollbar>
    </ASpin>
</template>

<style scoped>
.text-monitor-spin,
.text-monitor-scrollbar-outer,
.text-monitor-scrollbar-outer :deep(.text-monitor-scrollbar) {
    width: 100%;
    height: 100%;
}

.text-monitor-scrollbar-outer :deep(.text-monitor-scrollbar) {
    overflow: auto;
}

.text-wrapper {
    background-color: var(--color-bg-2);
    box-sizing: border-box;
    width: 100%;
    height: auto;
    min-height: 100%;
    padding: 1rem;
    word-break: normal;
    white-space: pre-wrap;
    word-wrap: break-word;
    line-height: 1.5;
}

.text-line {
    display: flex;
    display: -webkit-flex;
}

.serial {
    color: var(--color-text-3);
    font-size: .8em;
    line-height: 1.875;
    margin-right: 1em;
    user-select: none;
}

.text-content {
    flex-basis: 100%;
}
</style>