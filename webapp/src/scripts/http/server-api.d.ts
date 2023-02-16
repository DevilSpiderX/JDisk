import { AxiosResponse, AxiosProgressEvent } from "axios";
export interface Resp {
    code: number,
    msg: string,
    data: any,
    dataCount?: number
}

export interface Http {
    admin: {
        login: (username: string, password: string) => Promise<Resp>
    },
    systemConfig: {
        list: () => Promise<Resp>,
        update: (key: string, value: number | boolean | string) => Promise<Resp>
    },
    directLink: {
        list: () => Promise<Resp>,
        generate: (paths: Array<string>, driverKey: string) => Promise<Resp>,
        remove: (directKey: string) => Promise<Resp>
    },
    signature: {
        apply: (path: string, driverKey: string) => Promise<Resp>
    },
    driver: {
        list: () => Promise<Resp>,
        operate: {
            add: (
                name: string,
                key: string,
                path: string,
                remark: string,
                isPrivate: boolean,
                tokenTime: number,
                enableFileOperator: boolean
            ) => Promise<Resp>,
            remove: (id: number) => Promise<Resp>,
            update: (
                id: number,
                enable: boolean,
                name: string,
                key: string,
                path: string,
                remark: string,
                isPrivate: boolean,
                tokenTime: number,
                enableFileOperator: boolean
            ) => Promise<Resp>,
            scan: (id: number) => Promise<Resp>
        }
    },
    file: {
        list: () => Promise<Resp>,
        operate: {
            update: (path: string, newName: string, newParent: string, driverKey: string) => Promise<Resp>,
            remove: (path: string, driverKey: string) => Promise<Resp>,
            mkdir: (name: string, parent: string, driverKey: string) => Promise<Resp>,
            upload: (
                driverKey: string,
                parent: string,
                file: Blob,
                fileName: string,
                onUploadProgress: (progressEvent: AxiosProgressEvent) => void,
                abortSignal: AbortSignal
            ) => Promise<Resp>
        }
    }
}

declare const http: Http;

export default http;