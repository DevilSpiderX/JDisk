<script setup>
import { http } from "@/scripts/http";
import { useAppConfigs } from "@/store/AppConfigsStore";
import { useDriverList } from "@/store/DriverList";
import { Message } from "@arco-design/web-vue";
import { computed, reactive, ref,onMounted } from 'vue';

const appConfigs = useAppConfigs(), driverList = useDriverList();

const props = defineProps({
    id: {
        type: Number,
        default: -1
    }
});

onMounted(driverList.updateList);

const padding = computed(() => appConfigs.client.width < 768 ? "0" : "30px 0");

const card_loading = ref(false);
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
                                驱动器信息
                            </template>
                        </ACardMeta>
                    </ACard>
                </ACol>
            </ARow>
        </ALayoutContent>
    </ALayout>
</template>

<style scoped></style>