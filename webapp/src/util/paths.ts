
export function constructPath(paths?: Array<string>) {
    if (paths) {
        let path = "";
        let parentPath;
        for (const p of paths) {
            if (p === "") continue;
            parentPath = path;
            path += `/${p}`
        }
        if (!parentPath) parentPath = "/";
        return { path, parentPath };
    } else {
        return { path: "/", parentPath: undefined };
    }
}

export function getSuffix(fileName: string) {
    const l = fileName.split(".");
    return l[l.length - 1];
}