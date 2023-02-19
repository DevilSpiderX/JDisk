import { defineStore } from 'pinia';
import { ref } from 'vue';

interface ValuesType {
    installed?: boolean,
    siteName?: string,
    username?: string,
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
    const values = ref<ValuesType>({});

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