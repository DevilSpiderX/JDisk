<script setup>
import { http } from "@/scripts/http";
import { useAppConfigs } from "@/store/AppConfigsStore";
import { useSystemConfigs } from "@/store/SystemConfigs";
import { Message } from "@arco-design/web-vue";
import Hex from "crypto-js/enc-hex";
import SHA256 from "crypto-js/sha256";
import { computed, reactive, ref } from 'vue';
import { useRouter } from "vue-router";

const appConfigs = useAppConfigs(), systemConfigs = useSystemConfigs(), router = useRouter();

const padding = computed(() => appConfigs.client.width < 768 ? "0" : "30px 0");

const card_loading = ref(false);

const formLayout = computed(() => appConfigs.client.width < 768 ? "vertical" : "horizontal");

const form = reactive({
    username: systemConfigs.values.username,
    password: "",
    password2: ""
});

function checkPassword() {
    return form.password === form.password2;
}

async function form_submit() {
    console.log(form);
    for (const key in form) {
        if (form[key] === "") {
            Message.error("必填项不能为空");
            return;
        }
    }

    if (!checkPassword()) {
        Message.error("两次密码不同");
        return;
    }

    const password = SHA256(form.password).toString(Hex);

    const resp = await http.systemConfig.update("username", form.username);
    const resp1 = await http.systemConfig.update("password", password);

    if (resp.code !== 0) {
        Message.error("用户名更改失败");
    }
    if (resp1.code !== 0) {
        Message.error("密码更改失败");
    }
    if (resp.code === 0 && resp1.code === 0) {
        Message.success("更改成功");
        router.push({ name: "admin_normal" });
    }
}

const passwordErrorStatus = computed(() => !checkPassword());


</script>

<template>
    <ALayout>
        <ALayoutContent>
            <ARow justify="center" :style="{ padding }">
                <ACol :xs="24" :md="20">
                    <ACard :loading="card_loading">
                        <ACardMeta title="密码信息" description="此处可以修改您的管理员登录账号密码，请妥善保管" style="margin-bottom: 1rem" />
                        <AForm :model="form" :layout="formLayout" label-align="left" @submit="form_submit">
                            <AFormItem field="username" label="管理员账号" required :wrapper-col-props="{ xs: 24, md: 17 }">
                                <AInput v-model="form.username" allow-clear>
                                    <template #prefix>
                                        <span><i class="fa-solid fa-user"></i></span>
                                    </template>
                                </AInput>
                            </AFormItem>
                            <AFormItem field="password" label="新密码" required :wrapper-col-props="{ xs: 24, md: 17 }">
                                <AInputPassword v-model="form.password" :error="passwordErrorStatus" allow-clear>
                                    <template #prefix>
                                        <span><i class="fa-duotone fa-key"></i></span>
                                    </template>
                                </AInputPassword>
                            </AFormItem>
                            <AFormItem field="password2" label="重复新密码" required :wrapper-col-props="{ xs: 24, md: 17 }">
                                <AInputPassword v-model="form.password2" :error="passwordErrorStatus" allow-clear>
                                    <template #prefix>
                                        <span><i class="fa-duotone fa-key"></i></span>
                                    </template>
                                </AInputPassword>
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