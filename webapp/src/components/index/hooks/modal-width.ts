import { computed } from "vue";
import { useAppConfigs } from "@/store/AppConfigsStore";

export function useModalWidth() {
    const appConfigs = useAppConfigs();

    const width = computed(() => {
        const winWidth = appConfigs.client.width;
        if (winWidth < 576) {
            //xs [0, 576)
            return "90%";
        } else if (winWidth < 768) {
            //sm [576, 768)
            return "80%";
        } else if (winWidth < 992) {
            //md [768, 992)
            return "75%";
        } else if (winWidth < 1200) {
            //lg [992, 1200)
            return "70%";
        } else {
            //xl & xxl [1200, ∞)
            return "800px";
        }
    });

    return {
        width
    };
}