import { defineStore } from 'pinia';
import { ref } from 'vue';
import { http } from "@/scripts/http";

interface ValueType {
    id: number,
    enable: boolean,
    name: string,
    key: string,
    path: string,
    remark?: string,
    enableFileOperator: boolean,
    isPrivate: boolean,
    tokenTime: number,
}

export const useDriverList = defineStore("driverList", () => {
    const value = ref<Array<ValueType>>([]);

    async function updateList() {
        const resp = await http.driver.list();
        if (resp.code === 0) {
            value.value = resp.data;
        }
    }

    updateList();

    function getDriver(id: number) {
        for (const driver of value.value) {
            if (driver.id === id) {
                return driver;
            }
        }
        return null;
    }

    return {
        value,
        updateList,
        getDriver
    }
});