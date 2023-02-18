<script setup>
import { http } from "@/scripts/http";
import { Message } from "@arco-design/web-vue";
import Hex from "crypto-js/enc-hex";
import SHA256 from "crypto-js/sha256";
import { reactive, ref } from 'vue';
import { useRouter } from "vue-router";

const form = reactive({
    siteName: "JDisk",
    username: "",
    password: "",
    domain: location.origin
})

const inputStatus = ref([false, false, false, false]);

const router = useRouter();

async function form_submit() {
    if (form.siteName === "") {
        inputStatus.value[0] = true;
        return;
    }
    if (form.username === "") {
        inputStatus.value[1] = true;
        return;
    }
    if (form.password === "") {
        inputStatus.value[2] = true;
        return;
    }
    if (form.domain === "" || !checkDomain(form.domain)) {
        inputStatus.value[3] = true;
        return;
    }
    inputStatus.value = [false, false, false, false];

    const password = SHA256(form.password).toString(Hex);

    const resp = await http.systemConfig.install(form.siteName, form.username, password, form.domain);
    if (resp.code === 0) {
        router.removeRoute("install");
        router.push("/");
    } else {
        Message.error("初始化失败，请重试");
    }
}

function checkDomain(domain) {
    return /^(https?:\/\/)[^\s:]+(:\d{1,5})?$/.test(domain);
}

function on_domain_input(value) {
    inputStatus.value[3] = !checkDomain(value);
}
</script>

<template>
    <ALayout>
        <ALayoutContent style="padding: 0; overflow: visible">
            <ARow justify="center">
                <ACol class="register-col">
                    <AForm :model="form" @submit="form_submit">
                        <h1 style="text-align: center; font-size: 2rem">
                            初始化网盘
                        </h1>
                        <AFormItem field="siteName" hide-label>
                            <AInput v-model="form.siteName" class="my-input" placeholder="站点名称" allow-clear
                                :input-attrs="{ style: { 'font-size': '1.1rem' } }" :error="inputStatus[0]">
                                <template #prefix>
                                    <span><i class="fa-duotone fa-browser"></i></span>
                                </template>
                            </AInput>
                        </AFormItem>
                        <AFormItem field="username" hide-label>
                            <AInput v-model="form.username" class="my-input" placeholder="账号" allow-clear
                                :input-attrs="{ style: { 'font-size': '1.1rem' } }" :error="inputStatus[1]">
                                <template #prefix>
                                    <span><i class="fa-solid fa-user"></i></span>
                                </template>
                            </AInput>
                        </AFormItem>
                        <AFormItem field="password" hide-label>
                            <AInput v-model="form.password" class="my-input" placeholder="密码" allow-clear
                                :input-attrs="{ style: { 'font-size': '1.1rem' } }" :error="inputStatus[2]">
                                <template #prefix>
                                    <span><i class="fa-duotone fa-key"></i></span>
                                </template>
                            </AInput>
                        </AFormItem>
                        <AFormItem field="domain" hide-label>
                            <AInput v-model="form.domain" class="my-input" placeholder="站点域名" allow-clear
                                :input-attrs="{ style: { 'font-size': '1.1rem' } }" :error="inputStatus[3]"
                                @input="on_domain_input">
                                <template #prefix>
                                    <span><i class="fa-duotone fa-house"></i></span>
                                </template>
                            </AInput>
                        </AFormItem>
                        <AButton long size="large" html-type="submit">提 交</AButton>
                    </AForm>
                </ACol>
            </ARow>
        </ALayoutContent>
    </ALayout>
</template>

<style scoped>
.register-col {
    border-radius: 2ex;
    max-width: 500px;
    margin-top: 100px;
    background-color: var(--color-bg-2);
    box-shadow: var(--el-box-shadow-dark);
    padding: 15px;
}

form > h1,
form > div {
    margin: 10px 0;
}

form > button {
    margin: 10px 0;
    height: 48px;
    font-size: 1.25rem;
}

.my-input {
    font-size: 1.1rem;
    height: 3rem;
}
</style>