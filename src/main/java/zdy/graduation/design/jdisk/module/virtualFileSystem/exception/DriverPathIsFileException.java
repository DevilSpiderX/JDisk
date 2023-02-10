package zdy.graduation.design.jdisk.module.virtualFileSystem.exception;

public class DriverPathIsFileException extends Exception {
    public DriverPathIsFileException() {
        super("驱动器路径指向一个文件而不是目录");
    }
}
