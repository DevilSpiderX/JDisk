<script setup>
import { http } from "@/scripts/http";
import { useAppConfigs } from "@/store/AppConfigsStore";
import { useDriverList } from "@/store/DriverList";
import { Message, Modal } from "@arco-design/web-vue";
import { computed, onMounted, reactive, ref } from 'vue';

const appConfigs = useAppConfigs(), driverList = useDriverList();

const padding = computed(() => appConfigs.client.width < 768 ? "0" : "30px 0");

const card_loading = ref(false);

onMounted(async () => {
    card_loading.value = true;
    await driverList.updateList();
    card_loading.value = false;
});

const spin_loading = reactive({});

async function scanDriver(id) {
    const driver = driverList.getDriver(id);

    if (driver === null) {
        Message.error("驱动器不存在");
        return;
    }

    spin_loading[driver.key] = true;
    const resp = await http.driver.operate.scan(id);
    if (resp.code === 0) {
        Message.success(`驱动器${driver.name}扫描完成`);
    } else {
        Message.error(`驱动器${driver.name}扫描失败,${resp.msg}`);
    }
    spin_loading[driver.key] = false;
}

function deleteDriver(id) {
    Modal.warning({
        title: "警告",
        titleAlign: "center",
        width: "auto",
        content: "确认删除？",
        hideCancel: false,
        onOk: async () => {
            const resp = await http.driver.operate.remove(id)
            if (resp.code === 0) {
                Message.success("删除成功");
                driverList.updateList();
            } else {
                Message.error("删除失败");
            }
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
                        <ACardMeta title="驱动器信息" description="此页可以维护您的驱动器信息" style="margin-bottom: 1rem" />
                        <ARow :gutter="[20, 20]">
                            <ACol v-for="driver in driverList.value" :xs="24" :md="12" :lg="8" :xl="6">
                                <ASpin :loading="spin_loading[driver.key]" tip="驱动器扫描中..." dot
                                    style="width: 100%;height: 100%">
                                    <ACard class="driver-card" hoverable>
                                        <div class="info-wrapper">
                                            <div class="image-wrapper">
                                                <img src="@/assets/driver.svg" width="80" height="80" />
                                            </div>
                                            <template v-if="!driver.remark || driver.remark === ''">
                                                <h3 class="driver-name">{{ driver.name }}</h3>
                                            </template>
                                            <ATooltip v-else :content="driver.remark">
                                                <h3 class="driver-name">{{ driver.name }}</h3>
                                            </ATooltip>
                                            <div class="tag-wrapper">
                                                <ATag v-if="driver.enable" color="green" size="large">启用</ATag>
                                                <ATag v-else color="red" size="large">停用</ATag>
                                            </div>
                                        </div>
                                        <AButtonGroup size="large">
                                            <AButton @click="$router.push(`./driver-edit/${driver.id}`)">
                                                <template #icon>
                                                    <i class="fa-solid fa-pen-to-square"></i>
                                                </template>
                                                编 辑
                                            </AButton>
                                            <ADropdown trigger="hover">
                                                <AButton>
                                                    <template #icon>
                                                        <i class="fa-solid fa-ellipsis"></i>
                                                    </template>
                                                    更 多
                                                </AButton>
                                                <template #content>
                                                    <ADoption @click="$router.push(`/${driver.key}`)">
                                                        <template #icon>
                                                            <i class="fa-solid fa-folder"></i>
                                                        </template>
                                                        访 问
                                                    </ADoption>
                                                    <ADoption @click="$router.push(`./driver-edit/${driver.id}`)">
                                                        <template #icon>
                                                            <i class="fa-solid fa-pen-to-square"></i>
                                                        </template>
                                                        编 辑
                                                    </ADoption>
                                                    <ADoption @click="scanDriver(driver.id)">
                                                        <template #icon>
                                                            <i class="fa-solid fa-folder-magnifying-glass"></i>
                                                        </template>
                                                        扫 描
                                                    </ADoption>
                                                    <ADoption @click="deleteDriver(driver.id)">
                                                        <template #icon>
                                                            <i class="fa-solid fa-trash-xmark"></i>
                                                        </template>
                                                        删 除
                                                    </ADoption>
                                                </template>
                                            </ADropdown>
                                        </AButtonGroup>
                                    </ACard>
                                </ASpin>
                            </ACol>
                            <ACol :xs="24" :md="12" :lg="8" :xl="6">
                                <ACard class="driver-card" hoverable>
                                    <div class="plus-wrapper" @click="$router.push('./driver-edit')">
                                        <span><i class="fa-solid fa-plus"></i></span>
                                    </div>
                                </ACard>
                            </ACol>
                        </ARow>
                    </ACard>
                </ACol>
            </ARow>
        </ALayoutContent>
    </ALayout>
</template>

<style scoped>
.driver-card {
    height: 17rem;
}

.driver-card :deep(.arco-card-body) {
    box-sizing: border-box;
    width: 100%;
    height: 100%;
}

.arco-btn-group {
    width: 100%;
}

.arco-btn-group > button {
    width: 50%;
}

.info-wrapper {
    box-sizing: border-box;
    width: 100%;
    height: calc(17rem - 68px);
    padding: 32px 32px 16px 32px;
    display: flex;
    flex-direction: column;
}

.image-wrapper {
    display: flex;
    justify-content: center;
    align-items: center;
}

.driver-name {
    font-weight: 500;
    text-align: center;
    margin: 8px 0 8px 0;
}

.tag-wrapper {
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
}

.plus-wrapper {
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    cursor: pointer;
}

.plus-wrapper i {
    font-size: 3rem;
}
</style>