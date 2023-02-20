<script setup>
import { formatBytes } from "@/util/format-util";
import { getSvgByTypeAndSuffix } from "@/util/svg-util";
import { computed } from "vue";

const props = defineProps(["column", "record", "rowIndex"]);

const imgSrc = computed(() => getSvgByTypeAndSuffix(props.record.type, props.record.suffixType));

const name = computed(() => props.record.name);
const modified = computed(() => props.record.modified);
const size = computed(() => {
    if (props.record.size) {
        return formatBytes(props.record.size, 2, " ");
    }
    return undefined;
});

function containsMyDataIndex(dataIndex) {
    return dataIndex === "name" || dataIndex === "modified" || dataIndex === "size"
}
</script>

<template>
    <td>
        <template v-if="containsMyDataIndex(column.dataIndex)">
            <span v-if="column.dataIndex === 'name'" class="arco-table-cell arco-table-cell-align-left">
                <span class="arco-table-td-content">
                    <img :src="imgSrc" width="22" height="22" />
                    {{ name }}
                </span>
            </span>

            <template v-if="column.dataIndex === 'modified'">
                <template v-if="modified">
                    <slot />
                </template>
                <span v-else class="arco-table-cell arco-table-cell-align-left">
                    <span class="arco-table-td-content">
                        -
                    </span>
                </span>
            </template>

            <span v-if="column.dataIndex === 'size'" class="arco-table-cell arco-table-cell-align-left">
                <span class="arco-table-td-content">
                    {{ size ? size : "-" }}
                </span>
            </span>
        </template>
        <template v-else>
            <slot />
        </template>
    </td>
</template>

<style scoped></style>