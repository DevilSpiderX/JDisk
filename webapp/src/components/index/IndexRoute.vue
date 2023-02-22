<script setup lang="tsx">
import { DSXMenu } from "@/components/dsx-menu";
import { http } from "@/scripts/http";
import { useAppConfigs } from "@/store/AppConfigsStore";
import { useDriverList, ValueType as DriverType } from "@/store/DriverList";
import { useSystemConfigs } from "@/store/SystemConfigs";
import { formatBytes } from "@/util/format-util";
import { constructPath, getSuffix } from "@/util/paths";
import { getSvgByTypeAndSuffix } from "@/util/svg-util";
import {
    CustomIcon as ACustomIcon,
    FileItem as AFileItem,
    LayoutHeader as ALayoutHeader,
    Message,
    Modal,
    RequestOption as ARequestOption,
    TableColumnData as ATableColumnData,
    TableData as ATableData,
    TableRowSelection as ATableRowSelection,
    Upload as AUpload
} from "@arco-design/web-vue";
import { computed, onMounted, onUnmounted, reactive, ref, watch, watchEffect } from "vue";
import { useRouter } from "vue-router";
import MyTr from "./components/MyTr.vue";
import { useTableBodyScrollWrap } from "./hooks/table-body-scroll-wrap";
import { useTableMenu } from "./hooks/table-menu";

const appConfigs = useAppConfigs(),
    systemConfigs = useSystemConfigs(),
    driverList = useDriverList(),
    router = useRouter();

const adminStatus = ref(false);

onMounted(async () => {
    const resp = await http.admin.status();
    console.log("Index,Status", resp);
    if (resp.code === 0) {
        adminStatus.value = resp.data;
    }
});

const props = defineProps<{
    driverKey?: string,
    paths?: Array<string>
}>();

watchEffect(async () => {
    const driverKey = props.driverKey;
    await driverList.updateList();
    if (driverKey) {
        const driver = driverList.getDriver(driverKey);
        if (driver !== null) {
            console.log(`当前正在访问驱动器(${driver.name})`);
        } else {
            console.log(`当前驱动器Key(${driverKey})不存在`);
            router.push("/");
        }
    } else {
        driverSelect.value = undefined;
        if (!systemConfigs.values.rootShowStorage) {
            for (const driver of driverList.value) {
                if (driver.enable) {
                    router.push("/" + driver.key);
                    break;
                }
            }
        }
    }
})

const driver = computed<DriverType | null>(() => {
    if (props.driverKey) {
        const driver = driverList.getDriver(props.driverKey);
        if (driver && driver.enable) {
            driverSelect.value = driver.key;
        }
        return driver;
    }
    return null;
});

const directoryPath = computed(() => {
    if (props.paths) {
        const result = constructPath(props.paths);
        return result.path;
    }
    return "/";
});
const lastDirectoryPath = computed(() => {
    if (props.paths) {
        const result = constructPath(props.paths);
        return result.lastDirectoryPath;
    }
});

const tableLoading = ref(false);

interface MyFile {
    driverId: number,
    modified: string,
    name: string,
    parent: string,
    path: string,
    size?: number,
    type: "F" | "D"
}

const fileList = ref<Array<MyFile>>([]);
watchEffect(() => {
    if (props.driverKey) {
        updateFileList(props.driverKey, directoryPath.value);
    }
});

async function updateFileList(driverKey: string, directoryPath: string) {
    tableLoading.value = true;
    const resp = await http.file.list(driverKey, directoryPath);
    if (resp.code === 0) {
        fileList.value = resp.data;
    }
    tableLoading.value = false;
}

const breadcrumbRoutes = computed<Array<{ path: string, label: string }>>(() => {
    const result: Array<{ path: string, label: string }> = [];
    result.push({ path: "/", label: "首页" });
    let p = "";
    if (driver.value) {
        p += ("/" + driver.value.key);
        result.push({ path: p, label: driver.value.name });
    }
    if (props.paths) {
        for (const path of props.paths) {
            if (path === "") continue;
            p += ("/" + path);
            result.push({ path: p, label: path });
        }
    }
    return result;
});

const headerRef = ref<InstanceType<typeof ALayoutHeader> | null>(null);
const headerResizeObserver = new ResizeObserver(() => {
    const rect = headerRef.value?.$el.getBoundingClientRect();
    headerHeight.value = rect ? rect.height : 0;
});
const headerHeight = ref(0);
onMounted(() => {
    if (headerRef.value && headerRef.value.$el) {
        headerResizeObserver.observe(headerRef.value.$el);
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

interface MyTableData extends ATableData {
    index: number,
    name: string,
    modified: string,
    size: string,
    type: "F" | "D" | "driver" | "back",
    suffixType?: "video" | "image" | "audio" | "text" | "normal"
}

const tableData = computed<Array<MyTableData>>(() => {
    const list: Array<MyTableData> = [];
    if (props.driverKey) {
        let name = "首页";
        if (props.paths) {
            if (lastDirectoryPath.value === "/") {
                name = driver.value ? driver.value.name : "/";
            } else {
                name = props.paths[props.paths.length - 2];
            }
        }
        list.push({
            index: -1,
            name,
            modified: "-",
            size: "-",
            type: "back",
            disabled: true
        });
        for (let index = 0; index < fileList.value.length; index++) {
            const file = fileList.value[index];

            let suffixType: "video" | "image" | "audio" | "text" | "normal" | undefined = undefined;
            if (file.type === "F") {
                suffixType = getFileSuffixType(getSuffix(file.name));
            }

            const size = file.size ? formatBytes(file.size, 2, " ") : "-";

            list.push({
                index,
                name: file.name,
                modified: file.modified,
                size,
                type: file.type,
                suffixType
            });
        }
        list.sort((a: MyTableData, b: MyTableData) => {
            const _a = getNumByType(a);
            const _b = getNumByType(b);
            return _a - _b;
        });
    } else {
        for (let index = 0; index < driverList.value.length; index++) {
            const driver = driverList.value[index];
            if (driver.enable) {
                list.push({
                    index,
                    name: driver.name,
                    modified: "-",
                    size: "-",
                    type: "driver",
                    disabled: true
                });
            }
        }
    }
    return list;
});

function getFileSuffixType(suffix: string) {
    if (systemConfigs.values.customVideoSuffix.indexOf(suffix) !== -1) {
        return "video";
    } else if (systemConfigs.values.customImageSuffix.indexOf(suffix) !== -1) {
        return "image";
    } else if (systemConfigs.values.customAudioSuffix.indexOf(suffix) !== -1) {
        return "audio";
    } else if (systemConfigs.values.customTextSuffix.indexOf(suffix) !== -1) {
        return "text";
    } else {
        return "normal";
    }
}

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

const tableColumns = computed(() => {
    if (appConfigs.client.width >= 600) {
        return [
            { title: "文件名", dataIndex: "name", slotName: "fileName" },
            { title: "修改时间", dataIndex: "modified" },
            { title: "大小", dataIndex: "size" }
        ];
    } else {
        return [
            { title: "文件名", dataIndex: "name", slotName: "fileName" },
            { title: "大小", dataIndex: "size" }
        ]
    }
});

function on_table_row_click(record: ATableData) {
    switch (record.type) {
        case "D": {
            const dir = fileList.value[record.index];
            router.push(`/${props.driverKey}${dir.path}`);
            break;
        }
        case "back": {
            if (!lastDirectoryPath.value) {
                router.push("/");
            } else if (lastDirectoryPath.value === "/") {
                router.push(`/${props.driverKey}`);
            } else {
                router.push(`/${props.driverKey}${lastDirectoryPath.value}`);
            }
            break;
        }
        case "F": {
            const file = fileList.value[record.index];
            console.log(file);
            Modal.info({
                title: "下载",
                titleAlign: "center",
                width: "auto",
                content: `确认下载${file.name}？`,
                hideCancel: false,
                onOk: () => {
                    download(file);
                }
            });
            break;
        }
        case "driver": {
            const driver = driverList.value[record.index];
            router.push("/" + driver.key);
            break;
        }
    }
}

async function download(file: MyFile) {
    if (driver.value) {
        const resp = await http.signature.apply(file.path, driver.value.key);
        if (resp.code === 0) {
            const sign = resp.data;
            const driverKey = encodeURI(driver.value.key);
            const filePath = encodeURI(file.path);
            const downloadLink = `${location.origin}/dl/${driverKey}${filePath}`
                + (sign === "" ? "" : `?signature=${sign}`);
            window.open(downloadLink);
            return;
        }
    }
    Message.error("下载失败");
}

const tableRowSelection = reactive<ATableRowSelection>({
    type: "checkbox",
    selectedRowKeys: [],
    showCheckedAll: true
});

watch(() => ({
    driverKey: props.driverKey,
    paths: props.paths
}), () => {
    tableRowSelection.selectedRowKeys = [];
});

function on_table_select(rowKeys: (string | number)[]) {
    tableMenu.visible = false;
    tableRowSelection.selectedRowKeys = rowKeys;
}

function on_table_select_all(checked: boolean) {
    if (checked) {
        for (let index = 0; index < fileList.value.length; index++) {
            tableRowSelection.selectedRowKeys?.push(index);
        }
    } else {
        tableRowSelection.selectedRowKeys = [];
    }
}

const drawer = reactive({
    visible: false,
    empty_form: {}
});

const { tableMenu } = useTableMenu();
const { tableBodyScrollWrap } = useTableBodyScrollWrap(".file-table");

function on_table_tr_contextmenu(record: MyTableData, rowIndex: number, event: PointerEvent) {
    //0打开 1下载  2生成直链  3分隔符  4重命名  5删除
    function setMenus3HiddenValue() {
        tableMenu.menus[3].hidden = (tableMenu.menus[4].hidden && tableMenu.menus[5].hidden) ||
            (tableMenu.menus[0].hidden && tableMenu.menus[1].hidden && tableMenu.menus[2].hidden);
    }

    if (driver.value) {
        tableMenu.menus[4].disabled = !driver.value.enableFileOperator;
        tableMenu.menus[5].disabled = !driver.value.enableFileOperator;
    }

    const selectedRowKeys = tableRowSelection.selectedRowKeys;

    if (selectedRowKeys && selectedRowKeys.length !== 0) {
        //有勾选项时
        tableMenu.menus[0].hidden = true;
        tableMenu.menus[1].hidden = true;
        tableMenu.menus[2].hidden = false;
        for (const index of selectedRowKeys) {
            if (typeof index === "number") {
                const file = fileList.value[index];
                if (file.type === "D") {
                    tableMenu.menus[2].hidden = true;
                    break;
                }
            }
        }
        tableMenu.menus[4].hidden = true;
        tableMenu.menus[5].hidden = !adminStatus.value;
        setMenus3HiddenValue();

        tableMenu.menus[5].label = `删除(${tableRowSelection.selectedRowKeys?.length})`;

        if (!tableMenu.menus[2].hidden) {
            tableMenu.onClicks.generateLink = async () => {
                if (props.driverKey) {
                    const fileNames: Array<string> = [];
                    const paths: Array<string> = [];
                    for (let i = 0; i < selectedRowKeys.length; i++) {
                        const index = selectedRowKeys[i];
                        if (typeof index === "number") {
                            const file = fileList.value[index];
                            fileNames.push(file.name);
                            paths.push(file.path);
                        }
                    }
                    const resp = await http.directLink.generate(paths, props.driverKey);
                    if (resp.code === 0) {
                        for (let i = 0; i < paths.length; i++) {
                            directLinkModal.table.data.push({
                                name: fileNames[i],
                                short: resp.data[i].shortLink,
                                long: resp.data[i].pathLink
                            });
                        }
                        directLinkModal.visible = true;
                    }
                }
            }
        }

        tableMenu.onClicks.delete = () => {
            Modal.warning({
                title: "警告",
                titleAlign: "center",
                width: "auto",
                content: `确认删除这${selectedRowKeys.length}个文件/目录？`,
                hideCancel: false,
                onOk: async () => {
                    tableLoading.value = true;
                    if (props.driverKey) {
                        for (let i = 0; i < selectedRowKeys.length; i++) {
                            const index = selectedRowKeys[i];
                            if (typeof index === "number") {
                                await http.file.operate.remove(fileList.value[index].path, props.driverKey);
                            }
                        }
                        await updateFileList(props.driverKey, directoryPath.value);
                    }
                    tableLoading.value = false;
                }
            });
        }

    } else {
        //没有勾选项时
        tableMenu.menus[0].hidden = record.type !== "D";
        tableMenu.menus[1].hidden = record.type !== "F";
        tableMenu.menus[2].hidden = record.type !== "F";
        tableMenu.menus[4].hidden = !adminStatus.value;
        tableMenu.menus[5].hidden = !adminStatus.value;
        setMenus3HiddenValue();

        tableMenu.menus[5].label = "删除";

        const file = fileList.value[record.index];

        if (record.type === "D") {
            tableMenu.onClicks.open = () => {
                if (props.driverKey) {
                    router.push(`/${props.driverKey}${file.path}`);
                }
            }
        }

        if (record.type === "F") {
            tableMenu.onClicks.download = () => {
                download(file);
            }

            tableMenu.onClicks.generateLink = async () => {
                if (props.driverKey) {
                    const resp = await http.directLink.generate([file.path], props.driverKey);
                    if (resp.code === 0) {
                        directLinkModal.table.data.push({
                            name: file.name,
                            short: resp.data[0].shortLink,
                            long: resp.data[0].pathLink
                        });
                        directLinkModal.visible = true;
                    }
                }
            }
        }

        tableMenu.onClicks.rename = () => {
            renameModal.visible = true;
            renameModal.form.name = file.name;
            renameModal.form.file = file;
        }

        tableMenu.onClicks.delete = () => {
            Modal.warning({
                title: "警告",
                titleAlign: "center",
                width: "auto",
                content: `确认删除这个${file.type === "F" ? "文件" : "目录"}( ${file.name} )？`,
                hideCancel: false,
                onOk: async () => {
                    tableLoading.value = true;
                    if (props.driverKey) {
                        await http.file.operate.remove(file.path, props.driverKey);
                        await updateFileList(props.driverKey, directoryPath.value);
                    }
                    tableLoading.value = false;
                }
            });
        }

    }

    //滚动表格消除右键菜单
    if (tableBodyScrollWrap.value)
        tableBodyScrollWrap.value.addEventListener("scroll", tableMenu_close, { once: true });
    //窗体尺寸变化消除右键菜单
    window.addEventListener("resize", tableMenu_close, { once: true });

    tableMenu.event = event;
    tableMenu.visible = true;
}

function tableMenu_close() {
    tableMenu.visible = false;
    removeTableMenuListener();
}

function removeTableMenuListener() {
    if (tableBodyScrollWrap.value)
        tableBodyScrollWrap.value.removeEventListener("scroll", tableMenu_close);
    window.removeEventListener("resize", tableMenu_close);
}

const mkdirModal = reactive({
    visible: false,
    form: {
        dirName: ""
    }
});

async function on_mkdirModal_ok() {
    const name = mkdirModal.form.dirName;
    if (name === "") {
        Message.error("必填项不能为空");
        return;
    }
    if (props.driverKey) {
        const resp = await http.file.operate.mkdir(name, directoryPath.value, props.driverKey);
        if (resp.code === 0) {
            Message.success("新建文件夹成功");
            await updateFileList(props.driverKey, directoryPath.value);
        }
    }
}

const directLinkModal = reactive<{
    visible: boolean,
    table: {
        columns: Array<ATableColumnData>,
        data: Array<ATableData>
    }
}>({
    visible: false,
    table: {
        columns: [
            { title: "文件名", dataIndex: "name", ellipsis: true, tooltip: { position: "tl" } },
            { title: "短链", dataIndex: "short", ellipsis: true, tooltip: { position: "top" } },
            { title: "长链", dataIndex: "long", ellipsis: true, tooltip: { position: "tr" } }
        ],
        data: []
    }
});

function on_directLink_modal_td_click(column: ATableColumnData, record: ATableData) {
    if (typeof navigator.clipboard === "object" && column.dataIndex) {
        navigator.clipboard.writeText(record[column.dataIndex]).then(() => {
            Message.success("复制成功");
        }).catch(() => {
            Message.error("复制失败");
        });
    }
}

const renameModal = reactive<{
    visible: boolean,
    form: {
        name: string,
        file: MyFile | null
    }
}>({
    visible: false,
    form: {
        name: "",
        file: null
    }
});

async function on_renamerModal_ok() {
    console.log(renameModal);
    if (renameModal.form.file && props.driverKey) {
        tableLoading.value = true;
        const resp = await http.file.operate.update(
            renameModal.form.file.path,
            renameModal.form.name,
            directoryPath.value,
            props.driverKey
        );
        if (resp.code === 0) {
            Message.success("重命名成功");
            await updateFileList(props.driverKey, directoryPath.value);
        }
    }
    Message.success("重命名失败");
    tableLoading.value = false;
}

const uploadMoadl = reactive<{
    visible: boolean,
    fileList: Array<AFileItem>,
    onClose: () => void,
    customIcon: ACustomIcon
}>({
    visible: false,
    fileList: [],
    onClose: () => {
        location.reload();
    },
    customIcon: {
        fileIcon: (fileItem: AFileItem) => {
            const suffixType = fileItem.name ? getFileSuffixType(getSuffix(fileItem.name)) : undefined;
            return <img src={getSvgByTypeAndSuffix("F", suffixType)} width="16" height="16" />
        }
    }
});

function uploadModalCustomRequest(option: ARequestOption) {
    const { onProgress, onError, onSuccess, fileItem } = option;
    const abortController = new AbortController()

    if (props.driverKey && fileItem.file) {
        http.file.operate.upload(
            props.driverKey,
            directoryPath.value,
            fileItem.file,
            fileItem.file.name,
            progressEvent => {
                if (progressEvent.progress) {
                    onProgress(progressEvent.progress, progressEvent.event);
                }
            },
            abortController.signal
        ).then(resp => {
            if (resp.code === 0) {
                onSuccess(resp);
            } else {
                onError(resp);
            }
        }).catch(error => {
            onError(error);
        });
    }

    return {
        abort() {
            abortController.abort();
        }
    }
}

const uploadingList = ref<Array<AFileItem>>([]);
const waitingList = ref<Array<AFileItem>>([]);
const uploadRef = ref<InstanceType<typeof AUpload> | null>(null);

function uploadFileItem(item: AFileItem) {
    if (uploadRef.value) {
        uploadRef.value.submit(item);
        uploadingList.value.push(item);
    }
}

function on_fileItme_status_change(_: Array<AFileItem>, item: AFileItem) {
    const maxFileUploads = systemConfigs.values.maxFileUploads;
    if (item.status === "init") {
        if (uploadingList.value.length < maxFileUploads) {
            uploadFileItem(item);
        } else {
            waitingList.value.push(item);
        }
    }
    if (item.status === "done" || item.status === "error") {
        const index = uploadingList.value.indexOf(item);
        uploadingList.value.splice(index, 1);

        const wItem = waitingList.value.shift()
        if (wItem) {
            uploadFileItem(wItem);
        }
    }
}

</script>

<template>
    <ALayout>
        <ALayoutHeader ref="headerRef">
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
                            <AButton @click="$router.push(adminStatus ? '/admin' : '/login')">
                                <template #icon>
                                    <i class="fa-solid fa-user-secret"></i>
                                </template>
                            </AButton>
                        </ATooltip>
                        <ADropdown v-if="adminStatus && driverKey">
                            <AButton>
                                <template #icon>
                                    <i class="fa-solid fa-cloud-plus"></i>
                                </template>
                            </AButton>
                            <template #content>
                                <ADoption :disabled="!driver?.enableFileOperator" @click="mkdirModal.visible = true">
                                    <template #icon>
                                        <i class="fa-solid fa-folder-plus"></i>
                                    </template>
                                    新建文件夹
                                </ADoption>
                                <ADoption :disabled="!driver?.enableFileOperator" @click="uploadMoadl.visible = true">
                                    <template #icon>
                                        <i class="fa-solid fa-file-arrow-up"></i>
                                    </template>
                                    上传文件
                                </ADoption>
                            </template>
                        </ADropdown>
                        <AButton @click="drawer.visible = true">
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
                            <ADoption v-if="systemConfigs.values.showLogin"
                                @click="$router.push(adminStatus ? '/admin' : '/login')">
                                <template #icon>
                                    <i class="fa-solid fa-user-secret"></i>
                                </template>
                                后台管理
                            </ADoption>
                            <template v-if="adminStatus && driverKey">
                                <ADoption :disabled="!driver?.enableFileOperator" @click="mkdirModal.visible = true">
                                    <template #icon>
                                        <i class="fa-solid fa-folder-plus"></i>
                                    </template>
                                    新建文件夹
                                </ADoption>
                                <ADoption :disabled="!driver?.enableFileOperator" @click="uploadMoadl.visible = true">
                                    <template #icon>
                                        <i class="fa-solid fa-file-arrow-up"></i>
                                    </template>
                                    上传文件
                                </ADoption>
                            </template>
                            <ADoption @click="drawer.visible = true">
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
            <div :style="contentScrollbarStyle">
                <ATable class="file-table" :columns="tableColumns" :data="tableData" row-key="index" :pagination="false"
                    :loading="tableLoading" @row-click="on_table_row_click" :scroll="{ y: '100%' }"
                    :row-selection="tableRowSelection" @select="on_table_select" @select-all="on_table_select_all">
                    <template #tr="{ record, rowIndex }">
                        <MyTr :record="record" :row-index="rowIndex" @contextmenu="on_table_tr_contextmenu" />
                    </template>
                    <template #fileName="{ record }">
                        <img :src="getSvgByTypeAndSuffix(record.type, record.suffixType)" width="22" height="22" />
                        {{ record.name }}
                    </template>
                    <template #empty>
                        <AEmpty style="cursor:default ;">
                            暂无驱动器可用，请先添加或启动至少一个驱动器
                        </AEmpty>
                    </template>
                </ATable>
            </div>
        </ALayoutContent>
    </ALayout>

    <DSXMenu v-model:visible="tableMenu.visible" :event="tableMenu.event" :menus="tableMenu.menus" min-width="100px"
        :style="tableMenu.style" :z-index="1002" />

    <ADrawer v-model:visible="drawer.visible" :footer="false">
        <template #header>
            <ARow justify="space-between" style="width: 100%;">
                <h2 style="margin: 0">
                    更多设置
                </h2>
                <AButton class="drawer-close-button" shape="circle" size="small" @click="drawer.visible = false">
                    <template #icon>
                        <IconClose />
                    </template>
                </AButton>
            </ARow>
        </template>
        <AForm :model="drawer.empty_form" auto-label-width>
            <template v-if="appConfigs.client.width < 768">
                <h3 style="color: var(--color-text-2)">
                    驱动器
                </h3>
                <AFormItem hide-label>
                    <ASelect v-model="driverSelect" placeholder="请选择驱动器" style="width: 200px"
                        @change="value => { $router.push(`/${value}`); drawer.visible = false; }">
                        <AOption v-for="d of driverList.value" :value="d.key" :disabled="!d.enable">
                            {{ d.name }}
                        </AOption>
                    </ASelect>
                </AFormItem>
            </template>
            <h3 style="color: var(--color-text-2)">
                主题
            </h3>
            <AFormItem label="深色模式">
                <ASwitch v-model="appConfigs.darkTheme" :disabled="appConfigs.themeFollowSystem" />
            </AFormItem>
            <AFormItem label="主题跟随系统">
                <ASwitch v-model="appConfigs.themeFollowSystem" />
            </AFormItem>
        </AForm>
    </ADrawer>

    <AModal title="新建文件夹" v-model:visible="mkdirModal.visible" @ok="on_mkdirModal_ok" :width="400">
        <AForm :model="mkdirModal.form" layout="vertical" label-align="left">
            <AFormItem field="dirName" label="文件夹名" required>
                <AInput v-model="mkdirModal.form.dirName">
                    <template #prefix>
                        <span><i class="fa-solid fa-folder-open"></i></span>
                    </template>
                </AInput>
            </AFormItem>
        </AForm>
    </AModal>

    <AModal title="直链" v-model:visible="directLinkModal.visible" width="auto" :footer="false"
        body-style="padding:0 0 24px 0" @close="directLinkModal.table.data = []">
        <ATable v-bind="directLinkModal.table" :pagination="false" :bordered="false">
            <template #td="{ column, record }">
                <td @click="on_directLink_modal_td_click(column, record)" :style="{ userSelect: 'none' }" />
            </template>
        </ATable>
    </AModal>

    <AModal title="重命名" v-model:visible="renameModal.visible" @ok="on_renamerModal_ok" :width="400">
        <AForm :model="renameModal.form" layout="vertical" label-align="left">
            <AFormItem field="name" label="新名字" required>
                <AInput v-model="renameModal.form.name">
                    <template #prefix>
                        <span><i class="fa-solid fa-file"></i></span>
                    </template>
                </AInput>
            </AFormItem>
        </AForm>
    </AModal>

    <AModal title="上传文件" v-model:visible="uploadMoadl.visible" width="90%" :footer="false" draggable
        @close="uploadMoadl.onClose">
        <AUpload v-model:file-list="uploadMoadl.fileList" ref="uploadRef" multiple draggable
            :custom-request="uploadModalCustomRequest" :auto-upload="false" :custom-icon="uploadMoadl.customIcon"
            @change="on_fileItme_status_change" />
    </AModal>
</template>

<style scoped>
.page-header {
    border-bottom: 1px solid var(--color-border-2);
}

.file-table :deep(tbody .arco-table-tr) {
    cursor: pointer;
}

.upload-item {
    display: flex;
}
</style>