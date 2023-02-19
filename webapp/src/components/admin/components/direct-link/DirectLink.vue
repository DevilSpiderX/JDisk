<script setup>
import { http } from "@/scripts/http";
import { useAppConfigs } from "@/store/AppConfigsStore";
import { useDriverList } from "@/store/DriverList";
import { Modal } from "@arco-design/web-vue";
import { computed, onMounted, reactive, ref, watch } from 'vue';

const appConfigs = useAppConfigs(), driverList = useDriverList();

const padding = computed(() => appConfigs.client.width < 768 ? "0" : "30px 0");

const card_loading = ref(false);

async function getDirectLinkList() {
    const resp = await http.directLink.list();
    if (resp.code === 0) {
        _tableData.value = resp.data;
    }
}

onMounted(async () => {
    card_loading.value = true;
    await driverList.updateList();
    await getDirectLinkList();
    card_loading.value = false;
});

const selectDriverId = ref();
const fileName = ref("");
const directKey = ref("");
const rangeDate = ref();

const _tableData = ref([]);

watch(() => _tableData.value, data => {
    tableData.value = getTableDataByCondition(data);
}, {
    deep: true
});

function getTableDataByCondition(source) {
    const result = [];
    for (const data of source) {
        const driver = driverList.getDriver(data.driverId);
        if (!driver.enable) continue;

        if (selectDriverId.value && selectDriverId.value !== data.driverId) continue;

        if (fileName.value !== "" && !data.path.endsWith(fileName.value)) continue;

        if (directKey.value !== "" && data.key !== directKey.value) continue;

        if (rangeDate.value && rangeDate.value.length === 2) {
            const date0 = new Date(rangeDate.value[0]);
            const date1 = new Date(rangeDate.value[1]);
            const createDate = new Date(data.createDate);
            if (createDate < date0 || createDate > date1) continue;
        }
        result.push({
            driverName: driver.name,
            directKey: data.key,
            path: data.path,
            date: data.createDate
        });
    }
    return result;
}

const tableData = ref([]);

const tableColumns = reactive([
    { title: "驱动器名称", dataIndex: "driverName", ellipsis: true, tooltip: { position: "tl" } },
    { title: "短链Key", dataIndex: "directKey", ellipsis: true, tooltip: true },
    { title: "路径", dataIndex: "path", ellipsis: true, tooltip: true },
    { title: "创建时间", dataIndex: "date", ellipsis: true, tooltip: true },
    { title: "操作", slotName: "operate" }
]);

const tableScroll = reactive({
    x: computed(() => {
        if (tableData.value.length === 0) {
            return "100%";
        }
        let winWidth = appConfigs.client.width;
        if (winWidth < 576) {
            //xs [0, 576)
            return "155%";
        } else if (winWidth < 768) {
            //sm [576, 768)
            return "140%";
        } else if (winWidth < 992) {
            //md [768, 992)
            return "125%";
        } else if (winWidth < 1200) {
            //lg [992, 1200)
            return "120%";
        } else {
            //xl & xxl [1200, ∞)
            return "100%";
        }
    })
});

const tableRowSelection = reactive({
    type: "checkbox",
    selectedRowKeys: [],
    showCheckedAll: true
});

function on_table_select(rowKeys) {
    tableRowSelection.selectedRowKeys = rowKeys;
}

function on_table_select_all(checked) {
    if (checked) {
        for (const data of tableData.value) {
            tableRowSelection.selectedRowKeys.push(data.directKey);
        }
    } else {
        tableRowSelection.selectedRowKeys = [];
    }
}

window.generate = http.directLink.generate;

const tableLoading = ref(false);

function on_search_button_click() {
    tableData.value = getTableDataByCondition(_tableData.value);
}

function getTableRowDataByKey(key) {
    let index = 0;
    for (const data of _tableData.value) {
        if (key === data.key) {
            return { index, data };
        }
        index++;
    }
    return { index, data: null }
}

function on_batch_delete_button_click() {
    const deletedKeys = tableRowSelection.selectedRowKeys;
    if (deletedKeys.length === 0) return;
    Modal.warning({
        title: "警告",
        titleAlign: "center",
        width: "auto",
        content: `确认删除这${deletedKeys.length}个直链？`,
        hideCancel: false,
        onOk: async () => {
            tableLoading.value = true;
            for (const key of deletedKeys) {
                await http.directLink.remove(key);
            }
            await getDirectLinkList();
            tableLoading.value = false;
        }
    });
}

function on_delete_button_click(record) {
    const directKey = record.directKey;
    Modal.warning({
        title: "警告",
        titleAlign: "center",
        width: "auto",
        content: `确认删除这个直链(${directKey})？`,
        hideCancel: false,
        onOk: async () => {
            tableLoading.value = true;
            const resp = await http.directLink.remove(directKey);
            if (resp.code === 0) {
                const { index } = getTableRowDataByKey(directKey);
                _tableData.value.splice(index, 1);
            }
            tableLoading.value = false;
        }
    });
}

</script>

<template>
    <ALayout>
        <ALayoutContent>
            <ARow justify="center" :style="{ padding }">
                <ACol :xs="24" :md="20">
                    <ACard :loading="card_loading">
                        <ACardMeta title="直链管理" style="margin-bottom: 1rem" />
                        <ARow :gutter="[20, 20]">
                            <ACol :xs="22" :sm="16" :md="12" :xl="8" :xxl="6">
                                <ARow align="center">
                                    <ACol :span="5">
                                        <label>驱动器</label>
                                    </ACol>
                                    <ACol :span="19">
                                        <ASelect v-model="selectDriverId" placeholder="请选择" allow-clear>
                                            <AOption v-for="driver of driverList.value" :value="driver.id"
                                                :disabled="!driver.enable">
                                                {{ driver.name }}
                                            </AOption>
                                        </ASelect>
                                    </ACol>
                                </ARow>
                            </ACol>
                            <ACol :xs="22" :sm="16" :md="12" :xl="8" :xxl="6">
                                <ARow align="center">
                                    <ACol :span="5">
                                        <label>文件名</label>
                                    </ACol>
                                    <ACol :span="19">
                                        <AInput v-model="fileName" allow-clear />
                                    </ACol>
                                </ARow>
                            </ACol>
                            <ACol :xs="22" :sm="16" :md="12" :xl="8" :xxl="6">
                                <ARow align="center">
                                    <ACol :span="5">
                                        <label>短链key</label>
                                    </ACol>
                                    <ACol :span="19">
                                        <AInput v-model="directKey" allow-clear />
                                    </ACol>
                                </ARow>
                            </ACol>
                            <ACol :xs="22" :sm="16" :md="12" :xl="8" :xxl="6">
                                <ARow align="center">
                                    <ACol :span="5">
                                        <label>创建时间</label>
                                    </ACol>
                                    <ACol :span="19">
                                        <ARangePicker v-model="rangeDate" />
                                    </ACol>
                                </ARow>
                            </ACol>
                            <ACol :xs="22" :sm="16" :md="12" :xl="8" :xxl="6">
                                <AButton type="primary" size="large" :disabled="tableLoading"
                                    @click="on_search_button_click">
                                    查 询
                                </AButton>
                            </ACol>
                        </ARow>
                        <div style="margin: 10px 0 10px 0">
                            <AButton type="primary" size="large" status="danger" @click="on_batch_delete_button_click"
                                :disabled="tableLoading">
                                批量删除
                            </AButton>
                        </div>
                        <ATable :columns="tableColumns" :data="tableData" row-key="directKey" :scroll="tableScroll"
                            :pagination="false" :row-selection="tableRowSelection" @select="on_table_select"
                            @select-all="on_table_select_all" :loading="tableLoading">
                            <template #operate="{ record }">
                                <AButton type="primary" status="danger" @click="on_delete_button_click(record)">
                                    删 除
                                </AButton>
                            </template>
                        </ATable>
                    </ACard>
                </ACol>
            </ARow>
        </ALayoutContent>
    </ALayout>
</template>

<style scoped></style>