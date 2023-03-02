import { http } from "@/scripts/http";
import { defineStore } from 'pinia';
import { ref } from 'vue';

export interface ValueType {
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
            console.log("更新驱动器列表:", resp);
        }
    }

    updateList();

    function getDriver(id_key: number | string) {
        if (typeof id_key === "number") {
            for (const driver of value.value) {
                if (driver.id === id_key) {
                    return driver;
                }
            }
        }
        if (typeof id_key === "string") {
            for (const driver of value.value) {
                if (driver.key === id_key) {
                    return driver;
                }
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