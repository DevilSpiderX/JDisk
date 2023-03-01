import { MenuItemOptionType } from "@/components/dsx-menu";
import { computed, ref } from "vue";
import { useRouter } from "vue-router";

const tableMenuStyle = {
    "--color-bg": "var(--color-bg-3)",
    "--color-border": "var(--color-border-2)"
}

const tableMenuItemStyle = {
    "--color-text": "var(--color-text-1)",
    "--color-bg-hover": "var(--color-secondary-hover)"
}

export function useTableMenu() {
    const visible = ref(false);
    const event = ref<MouseEvent | { x: number, y: number }>();

    const router = useRouter();

    const menuItems = ref<{
        preview: MenuItemOptionType,
        open: MenuItemOptionType,
        download: MenuItemOptionType,
        directLink: MenuItemOptionType,
        rename: MenuItemOptionType,
        delete: MenuItemOptionType,
        refresh: MenuItemOptionType,
    }>({
        preview: {
            label: "预览",
            onClick: () => { },
            style: tableMenuItemStyle,
            icon: (<i class="fa-solid fa-eye"></i>)
        },
        open: {
            label: "打开",
            onClick: () => { },
            style: tableMenuItemStyle,
            icon: (<i class="fa-solid fa-folder-open"></i>)
        },
        download: {
            label: "下载",
            onClick: () => { },
            style: tableMenuItemStyle,
            icon: (<i class="fa-solid fa-download"></i>)
        },
        directLink: {
            label: "生成直链",
            onClick: () => { },
            style: tableMenuItemStyle,
            icon: (<i class="fa-solid fa-link"></i>)
        },
        rename: {
            label: "重命名",
            onClick: () => { },
            style: tableMenuItemStyle,
            icon: (<i class="fa-solid fa-pen-to-square"></i>)
        },
        delete: {
            label: "删除",
            onClick: () => { },
            style: tableMenuItemStyle,
            icon: (<i class="fa-solid fa-trash"></i>)
        },
        refresh: {
            label: "刷新",
            onClick: () => {
                router.go(0);
            },
            style: tableMenuItemStyle,
            icon: (<i class="fa-solid fa-arrows-rotate"></i>)
        },
    });

    //@ts-ignore
    const menus = ref([
        menuItems.value.preview,
        menuItems.value.open,
        menuItems.value.download,
        menuItems.value.directLink,
        {
            divider: true,
            hidden: computed(() => (menuItems.value.preview.hidden && menuItems.value.open.hidden &&
                menuItems.value.download.hidden && menuItems.value.directLink.hidden) ||
                (menuItems.value.rename.hidden && menuItems.value.delete.hidden))
        },
        menuItems.value.rename,
        menuItems.value.delete,
        {
            divider: true,
            hidden: computed(() => (menuItems.value.preview.hidden && menuItems.value.open.hidden &&
                menuItems.value.download.hidden && menuItems.value.directLink.hidden &&
                menuItems.value.rename.hidden && menuItems.value.delete.hidden) ||
                menuItems.value.refresh.hidden)
        },
        menuItems.value.refresh,
    ]);

    return {
        visible,
        event,
        style: tableMenuStyle,
        menuItems,
        menus,
    }
}