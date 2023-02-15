package zdy.graduation.design.jdisk.module.virtualFileSystem.exception;

public class OperationNotAllowed extends Exception {
    public OperationNotAllowed(String name) {
        super("驱动器%s不允许操作".formatted(name));
    }
}
