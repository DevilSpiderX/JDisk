import { MenuItemOptionType } from "@/components/dsx-menu";
import { reactive, toRef, computed, ref, ComputedRef, Ref } from "vue";

const tableMenuStyle = {
    "--color-bg": "var(--color-bg-3)",
    "--color-border": "var(--color-border-2)"
}

const tableMenuItemStyle = {
    "--color-text": "var(--color-text-1)",
    "--color-bg-hover": "var(--color-secondary-hover)"
}

export function useTableMenu() {
    const menus = reactive<Array<MenuItemOptionType>>([
        { label: "打开", onClick: () => { }, style: tableMenuItemStyle, icon: (<i class="fa-solid fa-folder-open"></i>) },
        { label: "下载", onClick: () => { }, style: tableMenuItemStyle, icon: (<i class="fa-solid fa-download"></i>) },
        { label: "生成直链", onClick: () => { }, style: tableMenuItemStyle, icon: (<i class="fa-solid fa-link"></i>) },
        { divider: true },
        { label: "重命名", onClick: () => { }, style: tableMenuItemStyle, icon: (<i class="fa-solid fa-pen-to-square"></i>) },
        { label: "删除", onClick: () => { }, style: tableMenuItemStyle, icon: (<i class="fa-solid fa-trash"></i>) }
    ]);

    const tableMenu = reactive<{
        visible: boolean,
        event?: MouseEvent | { x: number, y: number },
        menus: typeof menus,
        onClicks: {
            open: any,
            download: any,
            generateLink: any,
            rename: any,
            delete: any
        },
        style: typeof tableMenuStyle
    }>({
        visible: false,
        event: undefined,
        menus,
        onClicks: {
            open: toRef(menus[0], "onClick"),
            download: toRef(menus[1], "onClick"),
            generateLink: toRef(menus[2], "onClick"),
            rename: toRef(menus[4], "onClick"),
            delete: toRef(menus[5], "onClick")
        },
        style: tableMenuStyle
    });

    return {
        tableMenu
    }
}