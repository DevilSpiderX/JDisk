<script setup>
import { useBodyNoScrollbar } from "@/hooks/body";
import { http } from "@/scripts/http";
import { useAppConfigs } from "@/store/AppConfigsStore";
import { useSystemConfigs } from "@/store/SystemConfigs";
import { Message, Scrollbar as AScrollbar } from "@arco-design/web-vue";
import { computed, reactive, watchEffect } from 'vue';
import { useRouter } from 'vue-router';

const appConfigs = useAppConfigs(),
    systemConfigs = useSystemConfigs(),
    router = useRouter();

const props = defineProps({
    selectedKey: {
        type: String
    }
});

watchEffect(() => {
    if (!props.selectedKey) {
        router.push("/admin/normal");
    }
});

const avatarSrc = computed(() => {
    return systemConfigs.values.avatar;
});

const pageHeader = reactive({
    selectedKeys: computed({
        get: () => {
            return [props.selectedKey];
        },
        set: value => {
            if (value.length > 0) {
                router.push({ name: `admin_${value[0]}` });
            }
        }
    })
});

const collapseMenu = reactive({
    show: false
});

function on_update_password_click() {
    router.push({ name: "admin_updatePassword" });
}

async function on_logout_click() {
    const resp = await http.admin.logout();
    if (resp.code === 0) {
        Message.success("注销成功");
        router.push({ name: "login" });
    }
}

function back_to_index() {
    router.push("/");
}

useBodyNoScrollbar();

</script>

<template>
    <ALayout style="height: 100%">
        <ALayoutHeader>
            <!-- 大于768px时用这个导航栏 -->
            <nav v-if="appConfigs.client.width >= 768">
                <ARow class="page-header" align="stretch">
                    <ACol flex="auto">
                        <AMenu mode="horizontal" v-model:selected-keys="pageHeader.selectedKeys">
                            <AMenuItem key="logo" disabled style="padding: 0;margin-left: 24px;margin-right: 12px;">
                                <ARow class="logo" align="center">
                                    <img src="/favicon.svg" width="40" height="40" @click="back_to_index" />
                                    <span style="font-size: 1.2rem;font-weight: 700" @click="back_to_index">
                                        JDisk
                                    </span>
                                </ARow>
                            </AMenuItem>
                            <AMenuItem key="normal">基本设置</AMenuItem>
                            <AMenuItem key="driverList">驱动器设置</AMenuItem>
                            <AMenuItem key="display">显示设置</AMenuItem>
                            <AMenuItem key="directLink">直链管理</AMenuItem>
                        </AMenu>
                    </ACol>
                    <ACol flex="80px" class="avatar-wrapper">
                        <ADropdown position="br">
                            <AAvatar style="cursor: pointer">
                                <img v-if="avatarSrc" :src="avatarSrc" width="40" height="40" />
                                <i v-else class="fa-solid fa-user"></i>
                            </AAvatar>
                            <template #content>
                                <ADoption @click="on_update_password_click">修改密码</ADoption>
                                <ADoption @click="on_logout_click">注销</ADoption>
                            </template>
                        </ADropdown>
                    </ACol>
                </ARow>
            </nav>
            <!-- 小于768px时用这个导航栏 -->
            <APageHeader v-else class="page-header" @back="collapseMenu.show = !collapseMenu.show">
                <template #back-icon>
                    <span style="font-size: 1.2rem;position: relative">
                        <i class="fa-solid fa-bars fa-fw" />
                    </span>
                </template>
                <template #title>
                    <ARow class="logo" align="center" style="margin-left: 12px">
                        <img src="/favicon.svg" width="40" height="40" @click="back_to_index" />
                        <span style="font-size: 1.2rem;font-weight: 700" @click="back_to_index">
                            JDisk
                        </span>
                    </ARow>
                </template>
                <template #extra>
                    <ADropdown position="br">
                        <AAvatar style="cursor: pointer">
                            <img v-if="avatarSrc" :src="avatarSrc" width="40" height="40" />
                            <i v-else class="fa-solid fa-user"></i>
                        </AAvatar>
                        <template #content>
                            <ADoption @click="on_update_password_click">修改密码</ADoption>
                            <ADoption @click="on_logout_click">注销</ADoption>
                        </template>
                    </ADropdown>
                </template>
            </APageHeader>
        </ALayoutHeader>
        <!-- 这一块是可收缩框 -->
        <div v-if="appConfigs.client.width < 768">
            <Transition name="collapse">
                <div class="collapseMenu" v-show="collapseMenu.show">
                    <AMenu v-model:selected-keys="pageHeader.selectedKeys" @menu-item-click="collapseMenu.show = false">
                        <AMenuItem key="normal">基本设置</AMenuItem>
                        <AMenuItem key="driverList">驱动器设置</AMenuItem>
                        <AMenuItem key="display">显示设置</AMenuItem>
                        <AMenuItem key="directLink">直链管理</AMenuItem>
                    </AMenu>
                </div>
            </Transition>
        </div>
        <ALayoutContent style="height: 1px">
            <AScrollbar class="content-scrollbar" outer-class="content-scrollbar">
                <RouterView />
            </AScrollbar>
        </ALayoutContent>
    </ALayout>
</template>

<style scoped>
.page-header {
    border-bottom: 1px solid var(--color-border-2);
}

.logo {
    cursor: pointer;
    color: var(--color-text-1);
}

.avatar-wrapper {
    padding: 0 20px;
    background-color: var(--color-menu-light-bg);
    display: flex;
    align-items: center;
}

.collapseMenu {
    box-sizing: border-box;
    width: 100%;
    padding: 10px 10px 0;
    z-index: 101;
    background-color: var(--color-menu-light-bg);
    border-bottom: 1px solid #84858d55;
    position: absolute;
    overflow: hidden;
    transform-origin: top;
}

.collapse-enter-active,
.collapse-leave-active {
    transition: all 300ms ease-in-out;
}

.collapse-enter-from,
.collapse-leave-to {
    transform: scaleY(0);
}

.collapse-enter-to,
.collapse-leave-from {
    transform: scaleY(1);
}

:deep(.content-scrollbar) {
    overflow: auto;
    width: 100%;
    height: 100%;
    background-color: var(--color-secondary);
}
</style>