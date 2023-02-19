<script setup>
import AudioSvg from "@/assets/file-type-audio.svg";
import BackSvg from "@/assets/file-type-back.svg";
import FileSvg from "@/assets/file-type-file.svg";
import FolderSvg from "@/assets/file-type-folder.svg";
import ImageSvg from "@/assets/file-type-image.svg";
import RootSvg from "@/assets/file-type-root.svg";
import TextSvg from "@/assets/file-type-text.svg";
import VideoSvg from "@/assets/file-type-video.svg";
import { ref, computed } from "vue";
import { formatBytes } from "@/util/format-util";

const props = defineProps(["record", "rowIndex"]);

const imgSrc = computed(() => {
    const type = props.record.type;
    if (type === "F") {
        const suffixType = props.record.suffixType;
        if (suffixType === "video") {
            return VideoSvg;
        } else if (suffixType === "image") {
            return ImageSvg;
        } else if (suffixType === "audio") {
            return AudioSvg;
        } else if (suffixType === "text") {
            return TextSvg;
        } else {
            return FileSvg;
        }
    } else if (type === "D") {
        return FolderSvg;
    } else if (type === "back") {
        return BackSvg;
    } else if (type === "driver") {
        return RootSvg;
    } else {
        return FileSvg;
    }
})

const name = computed(() => props.record.name);
const modified = computed(() => props.record.modified);
const size = computed(() => {
    if (props.record.size) {
        return formatBytes(props.record.size, 2, " ");
    }
    return undefined;
});

</script>

<template>
    <tr class="arco-table-tr">
        <td data-v-7b04d353="" class="arco-table-td">
            <span class="arco-table-cell arco-table-cell-align-left">
                <span class="arco-table-td-content">
                    <img :src="imgSrc" width="22" height="22" />
                    {{ name }}
                </span>
            </span>
        </td>
        <td data-v-7b04d353="" class="arco-table-td">
            <span class="arco-table-cell arco-table-cell-align-left">
                <span class="arco-table-td-content">
                    {{ modified }}
                </span>
            </span>
        </td>
        <td data-v-7b04d353="" class="arco-table-td">
            <span class="arco-table-cell arco-table-cell-align-left">
                <span class="arco-table-td-content">
                    {{ size }}
                </span>
            </span>
        </td>
    </tr>
</template>

<style scoped></style>