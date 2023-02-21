<script setup>
const props = defineProps(["record", "rowIndex"]);
const emits = defineEmits(["contextmenu"])

function on_tr_contextmenu(event) {
    if (props.record.type !== "driver" && props.record.type !== "back") {
        event.preventDefault();
        emits("contextmenu", props.record, props.rowIndex, event);
    }
}

</script>

<template>
    <tr @contextmenu.stop="on_tr_contextmenu">
        <slot />
    </tr>
</template>