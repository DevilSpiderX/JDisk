package zdy.graduation.design.jdisk.module.virtualFileSystem.exception;

public class DriverScanning extends Exception {
    public DriverScanning(int id) {
        super("驱动器%d正在扫描".formatted(id));
    }
}
