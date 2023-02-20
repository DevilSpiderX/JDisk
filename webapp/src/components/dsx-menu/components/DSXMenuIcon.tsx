import { isVNode, VNodeChild } from "vue";

interface Props {
    icon: VNodeChild
}

const DSXMenuIcon = (props: Props) => {
    const { icon } = props;
    if (icon && isVNode(icon)) {
        return (<span class="icon-wrapper" >{icon}</span>);
    } else if (typeof icon === "string") {
        return (<span class="icon-wrapper" innerHTML={icon} />);
    }
}
DSXMenuIcon.props = ["icon"];

export default DSXMenuIcon;