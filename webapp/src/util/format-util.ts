const KB = 1024n
const MB = 1048576n
const GB = 1073741824n
const TB = 1099511627776n
const PB = 1125899906842624n
const EB = 1152921504606846976n

function unitBytes(n: number, scale: number = 2): { value: number, unit: string } {
    const precision = Math.pow(10, scale);
    const Precision = BigInt(precision);
    const N = BigInt(n) * Precision;
    let value, unit;
    if (n >= EB) {
        value = Number(N / EB) / precision;
        unit = "EB";
    } else if (n >= PB) {
        value = Number(N / PB) / precision;
        unit = "PB";
    } else if (n >= TB) {
        value = Number(N / TB) / precision;
        unit = "TB";
    } else if (n >= GB) {
        value = Number(N / GB) / precision;
        unit = "GB";
    } else if (n >= MB) {
        value = Number(N / MB) / precision;
        unit = "MB";
    } else if (n >= KB) {
        value = Number(N / KB) / precision;
        unit = "KB";
    } else {
        value = n;
        unit = "B";
    }
    return { value, unit };
}

function formatBytes(n: number, scale: number, delimiter: string = "") {
    let record = unitBytes(n, scale);
    let value = record.value.toFixed(scale);
    return `${value}${delimiter}${record.unit}`;
}

export { unitBytes, formatBytes };