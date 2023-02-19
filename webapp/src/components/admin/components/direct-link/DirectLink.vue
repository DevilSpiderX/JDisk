<script setup>
import { http } from "@/scripts/http";
import { useAppConfigs } from "@/store/AppConfigsStore";
import { useSystemConfigs } from "@/store/SystemConfigs";
import { Message } from "@arco-design/web-vue";
import { computed, reactive, ref } from 'vue';

const appConfigs = useAppConfigs(), systemConfigs = useSystemConfigs();

const padding = computed(() => appConfigs.client.width < 768 ? "0" : "30px 0");

const card_loading = ref(false);

</script>

<template>
    <ALayout>
        <ALayoutContent>
            <ARow justify="center" :style="{ padding }">
                <ACol :xs="24" :md="20">
                    <ACard :loading="card_loading">
                        <ACardMeta title="直链管理" style="margin-bottom: 1rem" />
                    </ACard>
                </ACol>
            </ARow>
        </ALayoutContent>
    </ALayout>
</template>

<style scoped>

</style>