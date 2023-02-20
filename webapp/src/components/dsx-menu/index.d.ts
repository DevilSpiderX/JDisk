import { DSXMenuIcon, DSXMenuItem } from "./components";
import DSXMenu from "./DSXMenu.vue";

export { DSXMenu, DSXMenuItem, DSXMenuIcon };

import { CSSProperties, VNodeChild } from "vue";

export interface MenuItemOptionType {
    label?: string,
    class?: string,
    style?: CSSProperties,
    icon?: VNodeChild,
    tip?: string,
    divider?: boolean,
    onClick?: (event: MouseEvent) => unknown,
    hidden?: boolean,
    disabled?: boolean
}

export interface DSXMenuProps {
    visible: boolean,
    event: { x: number, y: number } | MouseEvent,
    zIndex?: number,
    menus?: Array<MenuItemOptionType>,
    minWidth?: number | string;
    maxWidth?: number | string;
}

export interface DSXMenuItemProps {
    divider?: boolean,
    hidden?: boolean,
    disabled?: boolean
}
