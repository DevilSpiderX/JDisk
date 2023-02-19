import httpInstance from './http-instance';

const http = {
    admin: {
        async login(username, password) {
            const resp = await httpInstance.post("/api/admin/login", { username, password });
            return resp.data;
        },
        async logout() {
            const resp = await httpInstance.post("/api/admin/logout");
            return resp.data;
        },
        async status() {
            const resp = await httpInstance.post("/api/admin/status");
            return resp.data;
        }
    },
    systemConfig: {
        async list() {
            const resp = await httpInstance.get("/api/system/config/list");
            return resp.data;
        },
        async update(key, value) {
            const resp = await httpInstance.post("/api/system/config/update", { key, value });
            return resp.data;
        },
        async install(siteName, username, password, domain) {
            const postBody = {
                siteName, username, password, domain
            };
            const resp = await httpInstance.post("/api/system/config/install", postBody);
            return resp.data;
        }
    },
    directLink: {
        async list() {
            const resp = await httpInstance.get("/api/direct/link/list");
            return resp.data;
        },
        async generate(paths, driverKey) {
            const resp = await httpInstance.post("/api/direct/link/generate", { paths, driverKey });
            return resp.data;
        },
        async remove(directKey) {
            const resp = await httpInstance.post(`/api/direct/link/remove/${directKey}`);
            return resp.data;
        }
    },
    signature: {
        async apply(path, driverKey) {
            const resp = await httpInstance.post("/api/signature/apply", { path, driverKey });
            return resp.data;
        }
    },
    driver: {
        async list() {
            const resp = await httpInstance.get("/api/driver/list");
            return resp.data;
        },
        operate: {
            async add(name, key, path, remark, isPrivate, tokenTime, enableFileOperator) {
                const postBody = {
                    name,
                    key,
                    path,
                    remark,
                    isPrivate,
                    tokenTime,
                    enableFileOperator
                };
                const resp = await httpInstance.post("/api/driver/operate/add", postBody);
                return resp.data;
            },
            async remove(id) {
                const resp = await httpInstance.post(`/api/driver/operate/remove?id=${id}`);
                return resp.data;
            },
            async update(id, enable, name, key, path, remark, isPrivate, tokenTime, enableFileOperator) {
                const postBody = {
                    id,
                    enable,
                    name,
                    key,
                    path,
                    remark,
                    isPrivate,
                    tokenTime,
                    enableFileOperator
                };
                const resp = await httpInstance.post("/api/driver/operate/update", postBody);
                return resp.data;
            },
            async scan(id) {
                const resp = await httpInstance.post(`/api/driver/operate/scan?id=${id}`, undefined, {
                    timeout: 300000
                });
                return resp.data;
            }
        }
    },
    file: {
        async list(driverKey, dir) {
            const resp = await httpInstance.get(`/api/file/list/${driverKey}/${dir}`);
            return resp.data;
        },
        operate: {
            async update(path, newName, newParent, driverKey) {
                const postBody = {
                    path,
                    newName,
                    newParent,
                    driverKey
                };
                const resp = await httpInstance.post("/api/file/operate/update", postBody);
                return resp.data;
            },
            async remove(path, driverKey) {
                const resp = await httpInstance.post("/api/file/operate/remove", { path, driverKey });
                return resp.data;
            },
            async mkdir(name, parent, driverKey) {
                const resp = await httpInstance.post("/api/file/operate/mkdir", { name, parent, driverKey });
                return resp.data;
            },
            async upload(driverKey, parent, file, fileName, onUploadProgress, abortSignal) {
                const data = new FormData();
                data.append("file", file, fileName);
                data.append("fileName", fileName);
                const resp = await httpInstance.post(
                    `/api/file/operate/upload/${driverKey}/${parent}`,
                    data,
                    {
                        timeout: 0,
                        onUploadProgress,
                        signal: abortSignal
                    }
                );
                return resp.data;
            }
        }
    }
}

export default http;