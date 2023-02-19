import { defineStore } from 'pinia';
import { ref } from 'vue';

interface ValuesType {
    installed?: boolean,
    siteName?: string,
    domain?: string,
    avatar?: string,
    customVideoSuffix?: string,
    customImageSuffix?: string,
    customAudioSuffix?: string,
    customTextSuffix?: string,
    rootShowStorage?: boolean,
    maxFileUploads?: number,
    showLogin?: boolean
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
        showLogin: undefined
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