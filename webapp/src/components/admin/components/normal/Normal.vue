<script setup>
import { http } from "@/scripts/http";
import { useAppConfigs } from "@/store/AppConfigsStore";
import { useSystemConfigs } from "@/store/SystemConfigs";
import { Message } from "@arco-design/web-vue";
import { computed, reactive, ref } from 'vue';

const appConfigs = useAppConfigs(), systemConfigs = useSystemConfigs();

const padding = computed(() => appConfigs.client.width < 768 ? "0" : "30px 0");

const card_loading = ref(false);

const formLayout = computed(() => appConfigs.client.width < 768 ? "vertical" : "horizontal");

const form = reactive({
    siteName: systemConfigs.values.siteName,
    domain: systemConfigs.values.domain,
    avatar: systemConfigs.values.avatar,
    maxFileUploads: systemConfigs.values.maxFileUploads
});

async function form_submit() {
    card_loading.value = true;
    const flags = [];
    for (const key in form) {
        const value = form[key];
        if (value !== systemConfigs.values[key]) {
            const resp = await http.systemConfig.update(key, value);
            const flag = resp.code === 0;
            flags.push(flag)
            if (flag) {
                systemConfigs.values[key] = value;
            }
        }
    }
    card_loading.value = false;
    for (const flag of flags) {
        if (flag) {
            continue;
        } else {
            Message.error("保存失败");
            return;
        }
    }
    Message.success("保存成功");
}

const domainInputErrorStatus = computed(() => !checkDomain(form.domain));

function checkDomain(domain) {
    return /^(https?:\/\/)[^\s:]+(:\d{1,5})?$/.test(domain);
}

</script>

<template>
    <ALayout>
        <ALayoutContent>
            <ARow justify="center" :style="{ padding }">
                <ACol :xs="24" :md="20">
                    <ACard :loading="card_loading">
                        <ACardMeta title="站点信息" description="请填写您的站点信息" style="margin-bottom: 1rem" />
                        <AForm :model="form" :layout="formLayout" label-align="left" @submit="form_submit">
                            <AFormItem field="siteName" label="站点名称" required :wrapper-col-props="{ xs: 24, md: 17 }">
                                <AInput v-model="form.siteName">
                                    <template #prefix>
                                        <span><i class="fa-duotone fa-browser"></i></span>
                                    </template>
                                </AInput>
                            </AFormItem>
                            <AFormItem field="domain" label="站点域名" required :wrapper-col-props="{ xs: 24, md: 17 }">
                                <AInput v-model="form.domain" :error="domainInputErrorStatus">
                                    <template #prefix>
                                        <span><i class="fa-duotone fa-house"></i></span>
                                    </template>
                                </AInput>
                                <template #extra>
                                    此地址用于生成直链及本次存储下载使用，请务必保持和服务端地址一样 (需写 http(s):// 协议头)
                                </template>
                            </AFormItem>
                            <AFormItem field="avatar" label="头像地址" :wrapper-col-props="{ xs: 24, md: 17 }">
                                <AInput v-model="form.avatar">
                                    <template #prefix>
                                        <span><i class="fa-solid fa-user"></i></span>
                                    </template>
                                </AInput>
                                <template #extra>
                                    用于管理员页面右上角头像地址，推荐尺寸为 40 * 40
                                </template>
                            </AFormItem>
                            <AFormItem field="maxFileUploads" label="最大同时上传文件数">
                                <AInputNumber v-model="form.maxFileUploads" mode="button" :min="1"
                                    :input-attrs="{ style: { textAlign: 'center' } }" style="max-width: 150px" />
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