<script setup lang="ts">
import { useAppConfigs } from "@/store/AppConfigsStore";
import { useDriverList, ValueType as DriverType } from "@/store/DriverList";
import { useSystemConfigs } from "@/store/SystemConfigs";
import { constructPath, getSuffix } from "@/util/paths";
import { computed, onMounted, onUnmounted, reactive, ref, watchEffect, watch } from "vue";
import { useRouter } from "vue-router";
import { LayoutHeader, Modal } from "@arco-design/web-vue";
import { http } from "@/scripts/http";
import { Message, Scrollbar as AScrollbar, TableRowSelection, TableData } from "@arco-design/web-vue";
import MyTr from "./components/MyTr.vue";

const appConfigs = useAppConfigs(), systemConfigs = useSystemConfigs(), driverList = useDriverList()

const router = useRouter();

const props = defineProps<{
    driverKey?: string,
    paths?: Array<string>
}>();

const driver = ref<DriverType | null>(null);
watchEffect(async () => {
    if (props.driverKey) {
        await driverList.updateList();
        driver.value = driverList.getDriver(props.driverKey);
        if (driver.value === null || !driver.value.enable) {
            router.push("/");
        } else {
            driverSelect.value = driver.value.key;
        }
    } else if (!systemConfigs.values.rootShowStorage) {
        await driverList.updateList();

        check: {
            if (driverList.value.length === 0) {
                Message.error("没有驱动器，请添加后再试")
            } else {
                for (const d of driverList.value) {
                    if (d.enable) {
                        driver.value = d;
                        router.push("/" + d.key);
                        break check;
                    }
                }
                Message.error("没有启动的驱动器");
            }
        }
    }
});

const directoryPath = computed(() => {
    if (props.paths) {
        const result = constructPath(props.paths);
        return result.path;
    }
    return "/";
});
const parentPath = computed(() => {
    if (props.paths) {
        const result = constructPath(props.paths);
        return result.parentPath;
    }
});

const fileList = ref<Array<{
    driverId: number,
    modified: string,
    name: string,
    parent: string,
    path: string,
    size?: number,
    type: "F" | "D"
}>>([]);
watchEffect(async () => {
    if (driver.value && directoryPath.value) {
        const resp = await http.file.list(driver.value.key, directoryPath.value);
        if (resp.code === 0) {
            fileList.value = resp.data;
        }
    }
});

const breadcrumbRoutes = ref<Array<{ path: string, label: string }>>([]);

watchEffect(() => {
    breadcrumbRoutes.value = [];
    let p = "";
    if (systemConfigs.values.rootShowStorage && driver.value) {
        p += ("/" + driver.value.key);
        breadcrumbRoutes.value.push({ path: "/", label: "首页" });
        breadcrumbRoutes.value.push({ path: p, label: driver.value.name });
    } else if (driver.value) {
        p += ("/" + driver.value.key);
        breadcrumbRoutes.value.push({ path: p, label: "首页" });
    }
    if (props.paths) {
        for (const path of props.paths) {
            if (path === "") continue;
            p += ("/" + path);
            breadcrumbRoutes.value.push({ path: p, label: path });
        }
    }
});

const header = ref<typeof LayoutHeader | null>(null);
const headerResizeObserver = new ResizeObserver(() => {
    const rect = header.value?.$el.getBoundingClientRect();
    headerHeight.value = rect ? rect.height : 0;
});
const headerHeight = ref(0);
onMounted(() => {
    if (header.value && header.value.$el) {
        headerResizeObserver.observe(header.value.$el);
    }
});

onUnmounted(() => {
    headerResizeObserver?.disconnect();
});

const contentScrollbarStyle = reactive({
    overflow: "auto",
    width: "100%",
    height: computed(() => {
        const height = appConfigs.client.height - headerHeight.value;
        return height <= 0 ? "0" : `${height}px`;
    })
});

const driverSelect = ref();

interface MyTableData extends TableData {
    index: number,
    name: string,
    modified?: string,
    size?: number,
    type: "F" | "D" | "driver" | "back",
    suffixType?: "video" | "image" | "audio" | "text" | "normal"
}

const tableData = ref<Array<MyTableData>>([]);

const tableLoading = ref(false);

watchEffect(() => {
    if (props.driverKey) {
        const list: Array<MyTableData> = [];
        if (props.paths && parentPath.value) {
            let name = "";
            if (parentPath.value === "/") {
                name = "/";
            } else {
                name = props.paths[props.paths?.length - 2];
            }
            list.push({
                index: -1,
                name,
                type: "back"
            });
        }
        for (let index = 0; index < fileList.value.length; index++) {
            const file = fileList.value[index];

            let suffixType: "video" | "image" | "audio" | "text" | "normal" | undefined = undefined;
            if (file.type === "F") {
                const suffix = getSuffix(file.name);
                if (systemConfigs.values.customVideoSuffix?.indexOf(suffix) !== -1) {
                    suffixType = "video"
                } else if (systemConfigs.values.customImageSuffix?.indexOf(suffix) !== -1) {
                    suffixType = "image"
                } else if (systemConfigs.values.customAudioSuffix?.indexOf(suffix) !== -1) {
                    suffixType = "audio"
                } else if (systemConfigs.values.customTextSuffix?.indexOf(suffix) !== -1) {
                    suffixType = "text"
                } else {
                    suffixType = "normal"
                }
            }

            list.push({
                index,
                name: file.name,
                modified: file.modified,
                size: file.size,
                type: file.type,
                suffixType
            });
        }
        list.sort((a: MyTableData, b: MyTableData) => {
            const _a = getNumByType(a);
            const _b = getNumByType(b);
            return _a - _b;
        });
        tableData.value = list;
    } else {
        const list: Array<{ index: number, name: string, type: "driver" }> = [];
        for (let index = 0; index < driverList.value.length; index++) {
            const driver = driverList.value[index];
            list.push({
                index,
                name: driver.name,
                type: "driver"
            });
        }
        tableData.value = list;
    }
});

function getNumByType(a: MyTableData) {
    if (a.type === "back") {
        return 0;
    } else if (a.type === "driver") {
        return 1;
    } else if (a.type === "D") {
        return 2;
    } else if (a.type === "F") {
        return 3;
    } else {
        return 4;
    }
}

const tableColumns = reactive([
    { title: "文件名", dataIndex: "name" },
    { title: "修改时间", dataIndex: "modified" },
    { title: "大小", dataIndex: "size" }
]);

function on_table_row_click(record: TableData) {
    if (record.type === "D") {
        const dir = fileList.value[record.index];
        router.push(`/${driver.value?.key}${dir.path}`);
    } else if (record.type === "back") {
        if (parentPath.value === "/") {
            router.push(`/${driver.value?.key}`);
        } else {
            router.push(`/${driver.value?.key}${parentPath.value}`);
        }
    } else if (record.type === "F") {
        const file = fileList.value[record.index];
        console.log(file);
        Modal.info({
            title: "下载",
            titleAlign: "center",
            width: "auto",
            content: `确认下载${file.name}？`,
            hideCancel: false,
            onOk: async () => {
                if (driver.value) {
                    const resp = await http.signature.apply(file.path, driver.value.key);
                    if (resp.code === 0) {
                        const sign = resp.data;
                        let downloadLink;
                        if (sign === "") {
                            downloadLink = `${location.origin}/dl/${driver.value.key}${file.path}`;
                        } else {
                            downloadLink = `${location.origin}/dl/${driver.value.key}${file.path}?signature=${sign}`;
                        }
                        window.open(encodeURI(downloadLink));
                        return;
                    }
                }
                Message.error("下载失败");
            }
        });
    } else if (record.type === "driver") {
        const driver = driverList.value[record.index];
        router.push("/" + driver.key);
    }
}

</script>

<template>
    <ALayout>
        <ALayoutHeader ref="header">
            <APageHeader class="page-header" :show-back="false">
                <template #title>
                    <ARow class="logo" align="center" style="margin-left: 12px">
                        <img src="/favicon.svg" width="40" height="40" />
                        <span style="font-size: 1.2rem;font-weight: 700">
                            JDisk
                        </span>
                    </ARow>
                </template>
                <template #breadcrumb>
                    <ABreadcrumb>
                        <ABreadcrumbItem v-for="route of breadcrumbRoutes">
                            <ALink :href="route.path">
                                {{ route.label }}
                            </ALink>
                        </ABreadcrumbItem>
                    </ABreadcrumb>
                </template>
                <template #extra>
                    <ASpace v-if="appConfigs.client.width >= 768" size="large">
                        <ATooltip v-if="systemConfigs.values.showLogin" content="后台管理">
                            <AButton @click="$router.push('/admin')">
                                <template #icon>
                                    <i class="fa-solid fa-user-secret"></i>
                                </template>
                            </AButton>
                        </ATooltip>
                        <ADropdown v-if="driverKey">
                            <AButton>
                                <template #icon>
                                    <i class="fa-solid fa-cloud-plus"></i>
                                </template>
                            </AButton>
                            <template #content>
                                <ADoption :disabled="!driver?.enableFileOperator">
                                    <template #icon>
                                        <i class="fa-solid fa-folder-plus"></i>
                                    </template>
                                    新建文件夹
                                </ADoption>
                                <ADoption :disabled="!driver?.enableFileOperator">
                                    <template #icon>
                                        <i class="fa-solid fa-file-arrow-up"></i>
                                    </template>
                                    上传文件
                                </ADoption>
                                <ADoption :disabled="!driver?.enableFileOperator">
                                    <template #icon>
                                        <i class="fa-solid fa-folder-arrow-up"></i>
                                    </template>
                                    上传文件夹
                                </ADoption>
                            </template>
                        </ADropdown>
                        <AButton>
                            <template #icon>
                                <i class="fa-solid fa-gear"></i>
                            </template>
                        </AButton>
                        <ASelect v-model="driverSelect" placeholder="请选择驱动器" style="width: 200px"
                            @change="value => $router.push(`/${value}`)">
                            <AOption v-for="d of driverList.value" :value="d.key" :disabled="!d.enable">
                                {{ d.name }}
                            </AOption>
                        </ASelect>
                    </ASpace>
                    <ADropdown v-else>
                        <AButton>
                            <template #icon>
                                <i class="fa-solid fa-bars"></i>
                            </template>
                        </AButton>
                        <template #content>
                            <ADoption v-if="systemConfigs.values.showLogin" @click="$router.push('/admin')">
                                <template #icon>
                                    <i class="fa-solid fa-user-secret"></i>
                                </template>
                                后台管理
                            </ADoption>
                            <template v-if="driverKey">
                                <ADoption :disabled="!driver?.enableFileOperator">
                                    <template #icon>
                                        <i class="fa-solid fa-folder-plus"></i>
                                    </template>
                                    新建文件夹
                                </ADoption>
                                <ADoption :disabled="!driver?.enableFileOperator">
                                    <template #icon>
                                        <i class="fa-solid fa-file-arrow-up"></i>
                                    </template>
                                    上传文件
                                </ADoption>
                                <ADoption :disabled="!driver?.enableFileOperator">
                                    <template #icon>
                                        <i class="fa-solid fa-folder-arrow-up"></i>
                                    </template>
                                    上传文件夹
                                </ADoption>
                            </template>
                            <ADoption>
                                <template #icon>
                                    <i class="fa-solid fa-gear"></i>
                                </template>
                                更多设置
                            </ADoption>
                        </template>
                    </ADropdown>
                </template>
            </APageHeader>
        </ALayoutHeader>
        <ALayoutContent>
            <AScrollbar :style="contentScrollbarStyle">
                <ATable class="file-table" :columns="tableColumns" :data="tableData" row-key="index" :pagination="false"
                    :loading="tableLoading" @row-click="on_table_row_click">
                    <template #tr="{ record, rowIndex }">
                        <MyTr :record="record" :row-index="rowIndex" />
                    </template>
                </ATable>
            </AScrollbar>
        </ALayoutContent>
    </ALayout>
</template>

<style scoped>
.page-header {
    border-bottom: 1px solid var(--color-border-2);
}

.file-table :deep(tbody .arco-table-tr) {
    cursor: pointer;
}
</style>