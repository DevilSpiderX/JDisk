<script setup lang="ts">
import { computed } from 'vue';

interface Props {
    divider?: boolean,
    hidden?: boolean,
    disabled?: boolean
}

const props = defineProps<Props>();

const emit = defineEmits<{
    (e: "click", event: MouseEvent): void
}>();

const classObj = computed(() => {
    return [
        { "dsx-menu-item-disabled": props.disabled }
    ];
});

function item_click(event: MouseEvent) {
    if (props.disabled) {
        event.stopPropagation();
    } else {
        emit("click", event);
    }
}

</script>

<template>
    <div v-if="divider" v-show="!hidden" class="dsx-menu-item-divider" :class="classObj" @contextmenu.prevent />
    <div v-else v-show="!hidden" class="dsx-menu-item" :class="classObj" @click="item_click" @contextmenu.prevent>
        <div v-if="$slots.icon" class="dsx-menu-item-icon">
            <slot name="icon" />
        </div>
        <div class="dsx-menu-item-label">
            <slot />
        </div>
        <div v-if="$slots.suffix" class="dsx-menu-item-suffix">
            <slot name="suffix" />
        </div>
    </div>
</template>

<style scoped>
.dsx-menu-item-divider {
    height: 0;
    margin: .5rem 0;
    overflow: hidden;
    border-top: 1px solid var(--color-border);
}

.dsx-menu-item {
    --color-text: #16181b;
    --color-bg: #0000;
    --color-text-hover: var(--color-text);
    --color-bg-hover: #e9ecef;
    --color-text-suffix: #999;
    --color-text-disabled: #999;
}

.dsx-menu-item {
    box-sizing: border-box;
    display: flex;
    align-items: center;
    width: 100%;
    padding: .6rem 1rem;
    clear: both;
    font-weight: 400;
    color: var(--color-text);
    text-align: inherit;
    white-space: nowrap;
    background-color: var(--color-bg);
    border: 0;
}

.dsx-menu-item:focus,
.dsx-menu-item:hover {
    color: var(--color-text-hover);
    text-decoration: none;
    background-color: var(--color-bg-hover);
    cursor: pointer;
}

.dsx-menu-item-disabled {
    color: var(--color-text-disabled);
}

.dsx-menu-item-disabled:focus,
.dsx-menu-item-disabled:hover {
    color: var(--color-text-disabled);
    text-decoration: inherit;
    background-color: inherit;
    cursor: not-allowed;
}

.dsx-menu-item-icon {
    display: flex;
    margin-right: 0.6rem;
    min-width: 1rem;
}

.dsx-menu-item-label {
    flex: 1;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.dsx-menu-item-suffix {
    margin-left: 1.5rem;
    font-size: .3rem;
    color: var(--color-text-suffix);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}
</style>