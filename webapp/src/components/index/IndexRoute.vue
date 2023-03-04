<script setup lang="tsx">
import { DSXMenu } from "@/components/dsx-menu";
import { FileTypes, getFiletype } from "@/scripts/file-type";
import { http, httpInstance } from "@/scripts/http";
import { getFileSuffixType, SuffixTypes } from "@/scripts/suffix-type";
import { useAppConfigs } from "@/store/AppConfigsStore";
import { useDriverList, ValueType as DriverType } from "@/store/DriverList";
import { useSystemConfigs } from "@/store/SystemConfigs";
import { formatBytes } from "@/util/format-util";
import { constructPath, getDownloadURL, getSuffix } from "@/util/paths";
import { getSvgByTypeAndSuffix } from "@/util/svg-util";
import {
    CustomIcon as ACustomIcon,
    FileItem as AFileItem,
    LayoutHeader as ALayoutHeader,
    Message,
    Modal,
    RequestOption as ARequestOption,
    Scrollbar as AScrollbar,
    Table as ATable,
    TableColumnData as ATableColumnData,
    TableData as ATableData,
    TableRowSelection as ATableRowSelection,
    Upload as AUpload
} from "@arco-design/web-vue";
import { computed, onMounted, reactive, ref, watch, watchEffect } from "vue";
import { useRouter } from "vue-router";
import AudioPalyer from "./components/AudioPalyer.vue";
import MyTr from "./components/MyTr.vue";
import TextMonitor from "./components/TextMonitor.vue";
import VideoPreviewModal from "./components/VideoPreviewModal.vue";
import { useModalWidth } from "./hooks/modal-width";
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
    paths?: Array<string>;
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
});

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
    type: "F" | "D";
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
        console.log("更新文件列表：", resp);
    }
    tableRowSelection.selectedRowKeys = [];
    tableLoading.value = false;
}

const breadcrumbRoutes = computed<Array<{ path: string, label: string; }>>(() => {
    const result: Array<{ path: string, label: string; }> = [];
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

const driverSelect = ref();

const fileTableRef = ref<InstanceType<typeof ATable> | null>(null);
const { tableBodyScrollWrap, setTableScrollTop } = useTableBodyScrollWrap(fileTableRef);

interface MyTableData extends ATableData {
    index: number,
    name: string,
    modified: string,
    size: string,
    type: FileTypes,
    suffixType?: SuffixTypes;
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
            type: FileTypes.back,
            disabled: true
        });
        for (let index = 0; index < fileList.value.length; index++) {
            const file = fileList.value[index];

            let suffixType: SuffixTypes | undefined = undefined;
            if (file.type === "F") {
                suffixType = getFileSuffixType(getSuffix(file.name));
            }

            const size = file.size ? formatBytes(file.size, 2, " ") : "-";

            list.push({
                index,
                name: file.name,
                modified: file.modified,
                size,
                type: getFiletype(file.type),
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
                    type: FileTypes.driver,
                    disabled: true
                });
            }
        }
    }

    setTableScrollTop(0);
    return list;
});

function getNumByType(one: MyTableData) {
    switch (one.type) {
        case FileTypes.back: {
            return 0;
        }
        case FileTypes.driver: {
            return 1;
        }
        case FileTypes.directory: {
            return 2;
        }
        case FileTypes.file: {
            return 3;
        }
        default: {
            return 4;
        }
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
        ];
    }
});

// 单击文件
function on_table_row_click(record: ATableData) {
    switch (record.type) {
        case FileTypes.directory: {
            const dir = fileList.value[record.index];
            router.push(`/${props.driverKey}${dir.path}`);
            break;
        }
        case FileTypes.back: {
            if (!lastDirectoryPath.value) {
                router.push("/");
            } else if (lastDirectoryPath.value === "/") {
                router.push(`/${props.driverKey}`);
            } else {
                router.push(`/${props.driverKey}${lastDirectoryPath.value}`);
            }
            break;
        }
        case FileTypes.file: {
            const file = fileList.value[record.index];
            switch (record.suffixType) {
                case SuffixTypes.video: {
                    previewVideo(file);
                    break;
                }
                case SuffixTypes.image: {
                    previewImages(file);
                    break;
                }
                case SuffixTypes.text: {
                    previewText(file);
                    break;
                }
                case SuffixTypes.audio: {
                    previewAudio(file);
                    break;
                }
                default: {
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
            }
            break;
        }
        case FileTypes.driver: {
            const driver = driverList.value[record.index];
            router.push("/" + driver.key);
            break; 0;
        }
    }
}

// 下载文件
async function download(file: MyFile) {
    if (driver.value) {
        const resp = await http.signature.apply(file.path, driver.value.key);
        if (resp.code === 0) {
            const sign = resp.data;
            const downloadLink = getDownloadURL(
                `/dl/${driver.value.key}${file.path}`,
                { signature: sign === "" ? undefined : sign }
            );
            window.open(downloadLink);
            return;
        }
    }
    Message.error("下载失败");
}

// 预览图片文件
async function previewImages(currentImage: MyFile) {
    imagePreview.srcList = [];

    const imageList: Array<MyFile> = [];
    for (const data of tableData.value) {
        if (data.suffixType === SuffixTypes.image) {
            imageList.push(fileList.value[data.index]);
        }
    }
    if (driver.value) {
        let allPath = "";
        for (const img of imageList) {
            allPath += img.path;
        }

        const resp = await http.signature.apply(allPath, driver.value.key);
        if (resp.code === 0) {
            const sign = resp.data;
            for (let i = 0; i < imageList.length; i++) {
                const img = imageList[i];
                imagePreview.srcList.push(getDownloadURL(
                    `/dl/${driver.value.key}${img.path}`,
                    {
                        signature: sign === "" ? undefined : sign,
                        type: true
                    }
                ));
                if (img === currentImage) {
                    imagePreview.current = i;
                }
            }
            imagePreview.visible = true;
        }
    }
}

// 预览视频文件
async function previewVideo(currentVideo: MyFile) {
    if (driver.value) {
        const videoPath = currentVideo.path;
        const resp = await http.signature.apply(videoPath, driver.value.key);
        if (resp.code === 0) {
            const sign = resp.data;
            const videoURL = getDownloadURL(
                `/dl/${driver.value.key}${videoPath}`,
                {
                    signature: sign === "" ? undefined : sign,
                    type: true
                }
            );
            videoPreview.title = currentVideo.name;
            videoPreview.src = videoURL;
            videoPreview.visible = true;
        }
    }
}

// 预览文本文件
async function previewText(currentVideo: MyFile) {
    if (driver.value) {
        const textPath = currentVideo.path;
        const resp = await http.signature.apply(textPath, driver.value.key);
        if (resp.code === 0) {
            const sign = resp.data;
            const textURL = getDownloadURL(
                `/dl/${driver.value.key}${textPath}`,
                {
                    signature: sign === "" ? undefined : sign,
                    type: true
                }
            );
            textPreview.title = currentVideo.name;
            textPreview.visible = true;
            textPreview.loading = true;
            {
                const resp = await httpInstance.get(textURL, { transformResponse: data => data });
                textPreview.text = resp.data;
            }
            textPreview.loading = false;
        }
    }
}

// 预览音频文件
async function previewAudio(currentVideo: MyFile) {
    if (driver.value) {
        const audioPath = currentVideo.path;
        const resp = await http.signature.apply(audioPath, driver.value.key);
        if (resp.code === 0) {
            const sign = resp.data;
            const audioURL = getDownloadURL(
                `/dl/${driver.value.key}${audioPath}`,
                {
                    signature: sign === "" ? undefined : sign,
                    type: true
                }
            );
            audioPreview.title = currentVideo.name;
            audioPreview.srcStr = audioURL;
            audioPreview.visible = true;
        }
    }
}

// 文件选择器
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

// 右键菜单
const tableMenu = reactive(useTableMenu());

function on_table_tr_contextmenu(record: MyTableData, rowIndex: number, event: PointerEvent) {
    if (driver.value) {
        tableMenu.menuItems.rename.disabled = !driver.value.enableFileOperator;
        tableMenu.menuItems.delete.disabled = !driver.value.enableFileOperator;
    }

    const selectedRowKeys = tableRowSelection.selectedRowKeys;

    if (selectedRowKeys && selectedRowKeys.length !== 0) {
        //有勾选项时
        tableMenu.menuItems.preview.hidden = true;
        tableMenu.menuItems.open.hidden = true;
        tableMenu.menuItems.download.hidden = true;
        tableMenu.menuItems.directLink.hidden = false;
        for (const index of selectedRowKeys) {
            if (typeof index === "number") {
                const file = fileList.value[index];
                if (file.type === "D") {
                    tableMenu.menuItems.directLink.hidden = true;
                    break;
                }
            }
        }
        tableMenu.menuItems.rename.hidden = true;
        tableMenu.menuItems.delete.hidden = !adminStatus.value;

        tableMenu.menuItems.delete.label = `删除(${tableRowSelection.selectedRowKeys?.length})`;

        if (!tableMenu.menuItems.directLink.hidden) {
            tableMenu.menuItems.directLink.onClick = async () => {
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
            };
        }

        tableMenu.menuItems.delete.onClick = () => {
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
        };

    } else {
        //没有勾选项时
        tableMenu.menuItems.preview.hidden = record.type !== FileTypes.file || record.suffixType === SuffixTypes.normal;
        tableMenu.menuItems.open.hidden = record.type !== FileTypes.directory;
        tableMenu.menuItems.download.hidden = record.type !== FileTypes.file;
        tableMenu.menuItems.directLink.hidden = record.type !== FileTypes.file;
        tableMenu.menuItems.rename.hidden = !adminStatus.value;
        tableMenu.menuItems.delete.hidden = !adminStatus.value;
        tableMenu.menuItems.delete.label = "删除";

        const file = fileList.value[record.index];

        if (record.type === FileTypes.directory) {
            tableMenu.menuItems.open.onClick = () => {
                if (props.driverKey) {
                    router.push(`/${props.driverKey}${file.path}`);
                }
            };
        } else if (record.type === FileTypes.file) {
            tableMenu.menuItems.download.onClick = () => {
                download(file);
            };

            tableMenu.menuItems.directLink.onClick = async () => {
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
            };

            if (record.suffixType !== SuffixTypes.normal) {
                tableMenu.menuItems.preview.onClick = () => {
                    switch (record.suffixType) {
                        case SuffixTypes.video: {
                            previewVideo(file);
                            break;
                        }
                        case SuffixTypes.image: {
                            previewImages(file);
                            break;
                        }
                        case SuffixTypes.text: {
                            previewText(file);
                            break;
                        }
                        case SuffixTypes.audio: {
                            previewAudio(file);
                            break;
                        }
                    }
                };
            }
        }

        tableMenu.menuItems.rename.onClick = () => {
            renameModal.visible = true;
            renameModal.form.name = file.name;
            renameModal.form.file = file;
        };

        tableMenu.menuItems.delete.onClick = () => {
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
        };

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

// 设置抽屉
const drawer = reactive({
    visible: false,
    empty_form: {}
});

// 新建文件夹模态框
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

// 查看直链模态框
const directLinkModal = reactive<{
    visible: boolean,
    table: {
        columns: Array<ATableColumnData>,
        data: Array<ATableData>;
    };
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

// 重命名模态框
const renameModal = reactive<{
    visible: boolean,
    form: {
        name: string,
        file: MyFile | null;
    };
}>({
    visible: false,
    form: {
        name: "",
        file: null
    }
});

async function on_renamerModal_ok() {
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

// 文件上传模态框
const uploadMoadl = reactive<{
    visible: boolean,
    fileList: Array<AFileItem>,
    onClose: () => void,
    customIcon: ACustomIcon;
}>({
    visible: false,
    fileList: [],
    onClose: () => {
        if (props.driverKey) {
            updateFileList(props.driverKey, directoryPath.value);
        }
        uploadMoadl.fileList = [];
    },
    customIcon: {
        fileIcon: (fileItem: AFileItem) => {
            const suffixType = fileItem.name ? getFileSuffixType(getSuffix(fileItem.name)) : undefined;
            return <img src={getSvgByTypeAndSuffix(FileTypes.file, suffixType)} width="16" height="16" />;
        }
    }
});

function uploadModalCustomRequest(option: ARequestOption) {
    const { onProgress, onError, onSuccess, fileItem } = option;
    const abortController = new AbortController();

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
    };
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

        const wItem = waitingList.value.shift();
        if (wItem) {
            uploadFileItem(wItem);
        }
    }
}

// 图片预览组件
const imagePreview = reactive<{ visible: boolean, current: number, srcList: Array<string>; }>({
    visible: false,
    current: 0,
    srcList: []
});

// 视频预览组件
const videoPreview = reactive({
    title: "",
    visible: false,
    src: "",
    onClose: () => {
        videoPreview.src = "";
    }
});

// 文本预览组件
const textPreview = reactive({
    title: "",
    visible: false,
    text: "",
    loading: false,
    fullscreen: false,
    onClose: () => {
        textPreview.title = "";
        textPreview.text = "";
        textPreview.fullscreen = false;
    },
});

const computedWidth = useModalWidth().width;

const textPreviewModalWidth = computed(() => textPreview.fullscreen ? "100%" : computedWidth.value);

const textPreviewModalBodyStyle = computed(() => ({
    height: textPreview.fullscreen ? "100%" : "450px",
    padding: "0",
    overflow: "hidden"
}));

// 音频预览组件
const audioPreview = reactive({
    title: "",
    visible: false,
    srcStr: "",
    onClose: () => {
        audioPreview.title = "";
        audioPreview.srcStr = "";
    },
});

</script>

<template>
    <ALayout style="height: 100%">
        <ALayoutHeader>
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
                    <AScrollbar class="breadcrumb-scrollbar" outer-class="breadcrumb-scrollbar-outer">
                        <ABreadcrumb>
                            <ABreadcrumbItem v-for="route of breadcrumbRoutes">
                                <RouterLink :to="route.path">
                                    {{ route.label }}
                                </RouterLink>
                            </ABreadcrumbItem>
                        </ABreadcrumb>
                    </AScrollbar>
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

        <ALayoutContent style="height: 1px">
            <ATable class="file-table" :columns="tableColumns" :data="tableData" row-key="index" :pagination="false"
                :loading="tableLoading" @row-click="on_table_row_click" :scroll="{ y: '100%' }" ref="fileTableRef"
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
        </ALayoutContent>
    </ALayout>

    <!-- 右键菜单 -->
    <DSXMenu v-model:visible="tableMenu.visible" :event="tableMenu.event" :menus="tableMenu.menus" min-width="100px"
        :style="tableMenu.style" :z-index="1002" />

    <!-- 设置抽屉 -->
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

    <!-- 新建文件夹模态框 -->
    <AModal title="新建文件夹" v-model:visible="mkdirModal.visible" @ok="on_mkdirModal_ok" :width="400"
        @close="mkdirModal.form.dirName = ''">
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

    <!-- 查看直链模态框 -->
    <AModal title="直链" v-model:visible="directLinkModal.visible" width="90%" :footer="false" body-style="padding:0 0 24px 0"
        @close="directLinkModal.table.data = []">
        <ATable v-bind="directLinkModal.table" :pagination="false" :bordered="false">
            <template #td="{ column, record }">
                <td @click="on_directLink_modal_td_click(column, record)" :style="{ userSelect: 'none' }" />
            </template>
        </ATable>
    </AModal>

    <!-- 重命名模态框 -->
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

    <!-- 文件上传模态框 -->
    <AModal title="上传文件" v-model:visible="uploadMoadl.visible" width="90%" :footer="false" draggable
        @close="uploadMoadl.onClose">
        <AUpload v-model:file-list="uploadMoadl.fileList" ref="uploadRef" multiple draggable
            :custom-request="uploadModalCustomRequest" :auto-upload="false" :custom-icon="uploadMoadl.customIcon"
            @change="on_fileItme_status_change" />
    </AModal>

    <!-- 图片预览组件 -->
    <AImagePreviewGroup v-model:visible="imagePreview.visible" v-model:current="imagePreview.current" infinite
        :src-list="imagePreview.srcList" />

    <!-- 视频预览组件 -->
    <VideoPreviewModal :title="videoPreview.title" v-model:visible="videoPreview.visible" :videoSrc="videoPreview.src"
        @close="videoPreview.onClose" />

    <!-- 文本预览组件 -->
    <AModal v-model:visible="textPreview.visible" :width="textPreviewModalWidth" :footer="false" draggable
        :fullscreen="textPreview.fullscreen" :body-style="textPreviewModalBodyStyle" @close="textPreview.onClose">
        <TextMonitor :text="textPreview.text" :loading="textPreview.loading" />
        <template #title>
            <div class="text-title">{{ textPreview.title }}</div>
            <span class="full-screen-button arco-icon-hover" @click="textPreview.fullscreen = !textPreview.fullscreen">
                <i v-if="textPreview.fullscreen" class="fa-solid fa-compress arco-icon"></i>
                <i v-else class="fa-solid fa-expand arco-icon"></i>
            </span>
        </template>
    </AModal>

    <!-- 音频预览组件 -->
    <AModal :title="audioPreview.title" v-model:visible="audioPreview.visible" width="auto" :footer="false" draggable
        @close="audioPreview.onClose">
        <div class="audio-modal-body">
            <AudioPalyer :name="audioPreview.title" :src="audioPreview.srcStr" preload="metadata" autoplay />
        </div>
    </AModal>
</template>

<style scoped>
.page-header {
    border-bottom: 1px solid var(--color-border-2);
}

.breadcrumb-scrollbar-outer,
.breadcrumb-scrollbar-outer :deep(.breadcrumb-scrollbar) {
    width: 100%;
}

.breadcrumb-scrollbar-outer :deep(.breadcrumb-scrollbar) {
    overflow: auto;
}

.file-table :deep(tbody .arco-table-tr) {
    cursor: pointer;
}

.upload-item {
    display: flex;
}

.text-title {
    max-width: 270px;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
}

.full-screen-button {
    cursor: pointer;
    color: var(--color-text-1);
    font-size: 12px;
    position: absolute;
    right: 50px;
}

.audio-modal-body {
    display: flex;
    justify-content: center;
    align-items: center;

}
</style>