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
    showLogin: systemConfigs.values.showLogin,
    rootShowStorage: systemConfigs.values.rootShowStorage,
    customVideoSuffix: systemConfigs.values.customVideoSuffix,
    customImageSuffix: systemConfigs.values.customImageSuffix,
    customAudioSuffix: systemConfigs.values.customAudioSuffix,
    customTextSuffix: systemConfigs.values.customTextSuffix
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
</script>

<template>
    <ALayout>
        <ALayoutContent>
            <ARow justify="center" :style="{ padding }">
                <ACol :xs="24" :md="20">
                    <ACard :loading="card_loading">
                        <ACardMeta title="显示信息" description="此页面显示网站前台的显示相关的信息" style="margin-bottom: 1rem" />
                        <AForm :model="form" :layout="formLayout" label-align="left" @submit="form_submit">
                            <AFormItem field="showLogin" label="是否显示登录入口">
                                <ASwitch v-model="form.showLogin" />
                                <template #extra>
                                    启用后，会在门户显示后台登录入口，请根据自身情况选择是否启用.
                                </template>
                            </AFormItem>
                            <AFormItem field="rootShowStorage" label="根目录显示所有存储源">
                                <ASwitch v-model="form.rootShowStorage" />
                                <template #extra>
                                    根目录是否显示所有存储源, 如果为 true, 则根目录显示所有存储源列表, 如果为 false,
                                    则会自动跳转到第一个存储源.
                                </template>
                            </AFormItem>
                            <AFormItem field="customVideoSuffix" label="视频文件后缀" :wrapper-col-props="{ xs: 24, md: 17 }">
                                <AInput v-model="form.customVideoSuffix">
                                    <template #prefix>
                                        <span><i class="fa-solid fa-file-video"></i></span>
                                    </template>
                                </AInput>
                                <template #extra>
                                    自定义识别为视频格式的文件后缀，多个用逗号分开，如 'mp4,avi,mkv',
                                    在此列表中的将调用播放器打开（能否播放要取决于浏览器是否支持，
                                    现代浏览器一般只支持封装格式为 h.264 (mp4) 的编码格式）.
                                </template>
                            </AFormItem>
                            <AFormItem field="customImageSuffix" label="图像文件后缀" :wrapper-col-props="{ xs: 24, md: 17 }">
                                <AInput v-model="form.customImageSuffix">
                                    <template #prefix>
                                        <span><i class="fa-solid fa-file-image"></i></span>
                                    </template>
                                </AInput>
                            </AFormItem>
                            <AFormItem field="customAudioSuffix" label="音频文件后缀" :wrapper-col-props="{ xs: 24, md: 17 }">
                                <AInput v-model="form.customAudioSuffix">
                                    <template #prefix>
                                        <span><i class="fa-solid fa-file-audio"></i></span>
                                    </template>
                                </AInput>
                            </AFormItem>
                            <AFormItem field="customTextSuffix" label="文本文件后缀" :wrapper-col-props="{ xs: 24, md: 17 }">
                                <AInput v-model="form.customTextSuffix">
                                    <template #prefix>
                                        <span><i class="fa-solid fa-file-code"></i></span>
                                    </template>
                                </AInput>
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