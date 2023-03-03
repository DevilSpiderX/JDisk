
export function constructPath(paths?: Array<string>) {
    if (paths) {
        let path = "";
        let lastDirectoryPath;
        for (const p of paths) {
            if (p === "") continue;
            lastDirectoryPath = path;
            path += `/${p}`
        }
        if (!lastDirectoryPath) lastDirectoryPath = "/";
        return { path, lastDirectoryPath };
    } else {
        return { path: "/", lastDirectoryPath: undefined };
    }
}

export function getSuffix(fileName: string) {
    const l = fileName.split(".");
    return l[l.length - 1];
}

export function getDownloadURL(path: string, params?: Record<string, any>) {
    const url = new URL(encodeURI(path), location.origin);
    if (params) {
        for (const key in params) {
            if (Object.prototype.hasOwnProperty.call(params, key)) {
                const value = params[key];
                if (value) {
                    url.searchParams.append(key, value);
                }
            }
        }
    }
    return url.toString();
}