package zdy.graduation.design.jdisk.module.virtualFileSystem.exception;

public class DriverExistedException extends Exception {
    public DriverExistedException(String key) {
        super("虚拟驱动器%s已存在".formatted(key));
    }
}
