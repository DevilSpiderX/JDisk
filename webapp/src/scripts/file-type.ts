export enum FileTypes {
    file,
    directory,
    driver,
    back,
}

export function getFiletype(type: string) {
    switch (type) {
        case "D": {
            return FileTypes.directory;
        }
        case "F":
        default: {
            return FileTypes.file;
        }
    }
}