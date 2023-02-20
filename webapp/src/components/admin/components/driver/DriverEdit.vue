<script setup>
import { http } from "@/scripts/http";
import { useAppConfigs } from "@/store/AppConfigsStore";
import { useDriverList } from "@/store/DriverList";
import { Message, Modal } from "@arco-design/web-vue";
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from "vue-router";

const appConfigs = useAppConfigs(), driverList = useDriverList(), router = useRouter();

const props = defineProps({
    id: {
        type: Number,
        default: -1
    }
});

const driver = reactive({
    id: props.id,
    enable: true,
    name: "",
    key: "",
    path: "",
    remark: "",
    enableFileOperator: true,
    isPrivate: true,
    tokenTime: 1800,
});

const padding = computed(() => appConfigs.client.width < 768 ? "0" : "30px 0");

const card_loading = ref(false);

onMounted(async () => {
    card_loading.value = true;
    await driverList.updateList();
    card_loading.value = false;
    if (props.id !== -1) {
        const d = driverList.getDriver(props.id);
        if (d) {
            Object.assign(driver, d);
        } else {
            Message.error(`驱动器(id=${props.id})不存在`);
            router.push({ name: "admin_driverList" });
        }
    }
});

const formLayout = computed(() => appConfigs.client.width < 768 ? "vertical" : "horizontal");

async function form_submit() {
    if (driver.name === "" || driver.path === "") {
        Message.error("必填项不能为空");
        return;
    }
    if (driver.key === "") driver.key = driver.name;
    if (driver.tokenTime) driver.tokenTime = 1800;

    let flag = false;
    let msg;
    if (driver.id < 0) {
        const resp = await http.driver.operate.add(
            driver.name,
            driver.key,
            driver.path,
            driver.remark,
            driver.isPrivate,
            driver.tokenTime,
            driver.enableFileOperator
        );
        flag = resp.code === 0
        if (flag) {
            http.driver.operate.scan(resp.data.driverId);
        }
    } else {
        const resp = await http.driver.operate.update(
            driver.id,
            driver.enable,
            driver.name,
            driver.key,
            driver.path,
            driver.remark,
            driver.isPrivate,
            driver.tokenTime,
            driver.enableFileOperator
        );
        flag = resp.code === 0
        msg = resp.msg;
    }
    if (flag) {
        Modal.success({
            title: "保存成功",
            titleAlign: "center",
            width: "auto",
            content: "是否返回驱动器列表？",
            hideCancel: false,
            onOk: () => {
                router.push({ name: "admin_driverList" });
            },
            onCancel: () => {
                location.reload();
            }
        });
    } else {
        Message.error("保存失败");
        Message.error(msg);
    }
}

const keyExtraInfo = computed(() => `${location.origin}/${driver.key === "" ? "{ 驱动器别名 }" : driver.key}`);

function checkPath(path) {
    const winPattern = /^([a-zA-Z]:\\)[^\\\/:*?"<>|]*(\\[^\\\/:*?"<>|]+)*$/;
    const unixPattern = /^\/[^\\\/:*?"<>|]*(\/[^\\\/:*?"<>|]+)*$/;
    return winPattern.test(path) || unixPattern.test(path);
}

const pathInputErrorStatus = computed(() => !checkPath(driver.path));

</script>

<template>
    <ALayout>
        <ALayoutContent>
            <ARow justify="center" :style="{ padding }">
                <ACol :xs="24" :md="20">
                    <ACard :loading="card_loading">
                        <ACardMeta description="请维护您的驱动器信息" style="margin-bottom: 1rem">
                            <template #title>
                                <a href="javascript:void(0)" style="color: var(--color-text-3);text-decoration: none"
                                    @click="$router.push('/admin/driver-list')">
                                    <i class="fa-solid fa-arrow-left"></i>
                                </a>
                                &nbsp;
                                驱动器信息
                            </template>
                        </ACardMeta>
                        <AForm :model="driver" :layout="formLayout" label-align="left" @submit="form_submit">
                            <AFormItem field="name" label="驱动器名称" required :wrapper-col-props="{ xs: 24, md: 17 }">
                                <AInput v-model="driver.name">
                                    <template #prefix>
                                        <span><i class="fa-duotone fa-hard-drive"></i></span>
                                    </template>
                                </AInput>
                            </AFormItem>
                            <AFormItem field="key" label="驱动器别名" :wrapper-col-props="{ xs: 24, md: 17 }">
                                <AInput v-model="driver.key">
                                    <template #prefix>
                                        <span><i class="fa-solid fa-square-a"></i></span>
                                    </template>
                                </AInput>
                                <template #extra>
                                    驱动器别名，用于 URL 中展示, 如 {{ keyExtraInfo }}
                                </template>
                            </AFormItem>
                            <AFormItem field="remark" label="驱动器备注" :wrapper-col-props="{ xs: 24, md: 17 }">
                                <ATextarea v-model="driver.remark" allow-clear />
                                <template #extra>
                                    驱动器备注信息, 用于辅助管理员区分不同的驱动器, 此字段仅管理员可见.
                                </template>
                            </AFormItem>
                            <AFormItem field="enable" label="是否启用">
                                <ASwitch v-model="driver.enable" :disabled="driver.id < 0" />
                                <template #extra>
                                    如不启用，则在前台不显示，且不可访问.
                                </template>
                            </AFormItem>
                            <AFormItem field="path" label="文件路径" required :wrapper-col-props="{ xs: 24, md: 17 }">
                                <AInput v-model="driver.path" :error="pathInputErrorStatus">
                                    <template #prefix>
                                        <span><i class="fa-solid fa-folder-open"></i></span>
                                    </template>
                                </AInput>
                                <template #extra>
                                    只支持绝对路径.<br />
                                    Windows下路径以 C:\ D:\ 之类的开头，分隔符为 \ .<br />
                                    Unix，Linux下路径以 / 开头，分隔符为 / .
                                </template>
                            </AFormItem>
                            <AFormItem field="enableFileOperator" label="启用文件操作">
                                <ASwitch v-model="driver.enableFileOperator" />
                                <template #extra>
                                    是否启用文件上传，编辑，删除等操作.
                                </template>
                            </AFormItem>
                            <AFormItem field="isPrivate" label="生成签名链接">
                                <ASwitch v-model="driver.isPrivate" />
                                <template #extra>
                                    下载会生成带签名的下载链接, 如不想对外开放直链, 可以防止被当做直链使用.
                                </template>
                            </AFormItem>
                            <AFormItem field="tokenTime" label="下载签名有效期" :wrapper-col-props="{ xs: 24, md: 17 }">
                                <AInputNumber v-model="driver.tokenTime" :min="1">
                                    <template #prefix>
                                        <span><i class="fa-solid fa-timer"></i></span>
                                    </template>
                                </AInputNumber>
                                <template #extra>
                                    用于下载签名的有效期, 单位为秒, 如不配置则默认为 1800 秒.
                                </template>
                            </AFormItem>
                            <AFormItem>
                                <div style="width: 100%;display: flex;justify-content: end">
                                    <AButton type="primary" html-type="submit">
                                        保存设置
                                        <template #icon>
                                            <span><i class="fa-solid fa-badge-check"></i></span>
                                        </template>
                                    </AButton>
                                </div>
                            </AFormItem>
                        </AForm>
                    </ACard>
                </ACol>
            </ARow>
        </ALayoutContent>
    </ALayout>
</template>

<style scoped></style>