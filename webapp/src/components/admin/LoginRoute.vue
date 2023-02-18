<script setup>
import { http } from "@/scripts/http";
import { Message } from "@arco-design/web-vue";
import Hex from "crypto-js/enc-hex";
import SHA256 from "crypto-js/sha256";
import { onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";

const spin_loading = ref(false);

const form = reactive({
    username: "",
    password: ""
});

const inputStatus = reactive([false, false]);
const router = useRouter();

async function form_submit() {
    for (const i in inputStatus) inputStatus[i] = false;
    let username = form.username;
    let password = form.password;
    if (username === "") {
        inputStatus[0] = true;
        return;
    }
    if (password === "") {
        inputStatus[1] = true;
        return;
    }

    spin_loading.value = true;
    password = SHA256(password).toString(Hex);
    try {
        let resp = await http.admin.login(username, password);
        console.log("Login:", resp);
        switch (resp.code) {
            case 0: {
                router.push({ name: "admin" });
                break;
            }
            case 1: {
                Message.error("用户名密码错误");
                inputStatus[0] = true;
                inputStatus[1] = true;
                break;
            }
        }
    } catch (error) {
        console.error("(form_submit)", `url:${error.config?.url}`, error);
        Message.error("服务器错误");
    }
    spin_loading.value = false;
}

onMounted(async () => {
    const resp = await http.admin.status();
    console.log("Login,Status", resp);
    if (resp.code === 0 && resp.data) {
        router.push({ name: "admin" });
    }
});

</script>

<template>
    <ASpin :loading="spin_loading" style="width: 100%;height: 100%">
        <ALayout>
            <ALayoutContent style="padding: 0; overflow: visible">
                <ARow justify="center">
                    <ACol class="register-col">
                        <AForm :model="form" @submit="form_submit">
                            <h1 style="text-align: center; font-size: 2.2rem">
                                管 理 员 登 录
                            </h1>
                            <AFormItem field="username" hide-label>
                                <AInput v-model="form.username" class="my-input" placeholder="账号" allow-clear
                                    :input-attrs="{ style: { 'font-size': '1.1rem' } }" :error="inputStatus[0]">
                                    <template #prefix>
                                        <span><i class="fa-solid fa-user fa-fw"></i></span>
                                    </template>
                                </AInput>
                            </AFormItem>
                            <AFormItem field="password" hide-label>
                                <AInputPassword v-model="form.password" class="my-input" placeholder="密码" allow-clear
                                    :input-attrs="{ style: { 'font-size': '1.1rem' } }" :error="inputStatus[1]">
                                    <template #prefix>
                                        <span><i class="fa-duotone fa-key fa-fw"></i></span>
                                    </template>
                                </AInputPassword>
                            </AFormItem>
                            <ARow class="button-row" justify="space-around">
                                <AButton type="primary" size="large" html-type="submit">
                                    登 录
                                </AButton>
                                <AButton type="primary" size="large" html-type="button"
                                    @click="$router.push({ name: 'index' })">
                                    返 回
                                </AButton>
                            </ARow>
                        </AForm>
                    </ACol>
                </ARow>
            </ALayoutContent>
        </ALayout>
    </ASpin>
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

.button-row > button {
    margin: 10px 0;
    width: 100px;
    height: 50px;
    font-size: 1.25rem;
}

.my-input {
    font-size: 1.1rem;
    height: 3rem;
}
</style>