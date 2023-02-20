<script lang="ts">
import { defineComponent } from "vue";

export default defineComponent({
    name: "DSXMenu",
    inheritAttrs: false
});

</script>

<script setup lang="ts">
import { sleep } from '@/util/util';
import {
    computed,
    nextTick,
    onMounted,
    onUnmounted,
    reactive,
    ref,
    toRef,
    toRefs,
    watch
} from 'vue';
import { DSXMenuIcon as Icon, DSXMenuItem, MenuItemOptionType } from '.';

interface Props {
    visible: boolean,
    event: { x: number, y: number } | MouseEvent,
    zIndex?: number,
    menus?: Array<MenuItemOptionType>,
    minWidth?: number | string;
    maxWidth?: number | string;
}

const props = withDefaults(defineProps<Props>(), {
    visible: false,
    event: () => ({ x: 0, y: 0 }),
    zIndex: 100
});

const emit = defineEmits<{
    (e: "update:visible", visible: boolean): void
}>();

function close() {
    if (props.visible) {
        emit("update:visible", false);
    }
}

const isMoving = ref(false);

const classObj = computed(() => ({
    "dsx-menu-fade-enter-active": isMoving.value,
}));

const position = ref({
    x: 0,
    y: 0
});

watch(position, async () => {
    isMoving.value = true;
    await sleep(200);
    isMoving.value = false;
});

const transformOrigin = ref("left top");

const styleObj = computed(() => ({
    zIndex: props.zIndex,
    "--p-x": position.value.x + 'px',
    "--p-y": position.value.y + 'px',
    minWidth: props.minWidth ? typeof props.minWidth === "string" ? props.minWidth : props.minWidth + "px" : undefined,
    maxWidth: props.maxWidth ? typeof props.maxWidth === "string" ? props.maxWidth : props.maxWidth + "px" : undefined,
    "--t-origin": transformOrigin.value
}));

const DSX_Menu = ref<HTMLDivElement | null>(null);

watch(() => props.event, async event => {
    const clientWidth = document.documentElement.clientWidth;
    const clientHeight = document.documentElement.clientHeight;

    await nextTick();
    if (DSX_Menu.value) {
        const width = DSX_Menu.value.clientWidth;
        const height = DSX_Menu.value.clientHeight;

        const xRight = event.x + width;
        const yBottom = event.y + height;

        position.value = {
            x: xRight >= clientWidth ? event.x - width : event.x,
            y: yBottom >= clientHeight ? event.y - height : event.y
        };

        transformOrigin.value = xRight >= clientWidth ? "right " : "left ";
        transformOrigin.value += yBottom >= clientHeight ? "bottom" : "top";
    }
});

onMounted(() => {
    window.addEventListener("click", close);
});

onUnmounted(() => {
    window.removeEventListener("click", close);
});

function getItemBinds(item: MenuItemOptionType) {
    const { style, divider, hidden, disabled } = toRefs(item);
    return reactive({
        class: toRef(item, "class"),
        style,
        divider,
        hidden,
        disabled
    });
}

</script>

<template>
    <Teleport to="body">
        <Transition name='dsx-menu-fade'>
            <div v-if="visible" v-bind="$attrs" class="dsx-menu" :class="classObj" :style="styleObj" ref="DSX_Menu"
                @contextmenu.prevent="close">
                <div class="dsx-menu-body">
                    <template v-if="$slots.default">
                        <slot />
                    </template>
                    <template v-else>
                        <DSXMenuItem v-for="item in menus" v-bind="getItemBinds(item)" @click="item.onClick">
                            <!-- 图标 -->
                            <template #icon v-if="item.icon">
                                <Icon :icon="item.icon" />
                            </template>
                            <!-- 标签 -->
                            {{ item.label }}
                            <!-- 后缀 -->
                            <template #suffix v-if="item.tip">
                                {{ item.tip }}
                            </template>
                        </DSXMenuItem>
                    </template>
                </div>
            </div>
        </Transition>
    </Teleport>
</template>

<style scoped>
@import url(./styles/menu-transition.css);

.dsx-menu {
    --color-bg: #fff;
    --color-border: #00000026;
    --p-x: 0;
    --p-y: 0;
    --t-origin: left top;
}

.dsx-menu {
    position: fixed;
    top: 0;
    left: 0;
    z-index: 50;
    float: left;
    min-width: 1rem;
    padding: .5rem 0;
    font-size: .8rem;
    text-align: left;
    background-color: var(--color-bg);
    background-clip: padding-box;
    border: 1px solid var(--color-border);
    border-radius: .25rem;
    user-select: none;
    overflow: hidden;
    box-shadow: 0 .5rem 1rem rgba(0, 0, 0, .15);
    left: var(--p-x);
    top: var(--p-y);
    transform-origin: var(--t-origin);
}

.dsx-menu-body {
    display: block;
}
</style>