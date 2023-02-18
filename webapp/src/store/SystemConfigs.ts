import { defineStore } from 'pinia';
import { ref } from 'vue';

interface ValuesType {
    installed: boolean | undefined,
    siteName: string | undefined,
    domain: string | undefined,
    avatar: string | undefined,
    customVideoSuffix: string | undefined,
    customImageSuffix: string | undefined,
    customAudioSuffix: string | undefined,
    customTextSuffix: string | undefined,
    rootShowStorage: boolean | undefined,
    maxFileUploads: number | undefined
}

export const useSystemConfigs = defineStore("systemConfigs", () => {
    const values = ref<ValuesType>({
        installed: undefined,
        siteName: undefined,
        domain: undefined,
        avatar: undefined,
        customVideoSuffix: undefined,
        customImageSuffix: undefined,
        customAudioSuffix: undefined,
        customTextSuffix: undefined,
        rootShowStorage: undefined,
        maxFileUploads: undefined,
    });

    return {
        values
    }
}, {
    persist: {
        storage: sessionStorage,
        paths: [
            "values"
        ]
    }
});