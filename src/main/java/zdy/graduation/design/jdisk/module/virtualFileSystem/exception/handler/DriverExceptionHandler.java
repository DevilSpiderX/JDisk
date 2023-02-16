package zdy.graduation.design.jdisk.module.virtualFileSystem.exception.handler;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import zdy.graduation.design.jdisk.core.util.AjaxResp;
import zdy.graduation.design.jdisk.module.virtualFileSystem.exception.DriverExistedException;
import zdy.graduation.design.jdisk.module.virtualFileSystem.exception.DriverPathIsFileException;
import zdy.graduation.design.jdisk.module.virtualFileSystem.exception.OperationNotAllowed;

@ControllerAdvice
public class DriverExceptionHandler {
    @ExceptionHandler({
            DriverExistedException.class,
            DriverPathIsFileException.class
    })
    @ResponseBody
    public AjaxResp<Void> handlerDriverException(Exception e) {
        return AjaxResp.error(e.getMessage());
    }

    @ExceptionHandler({OperationNotAllowed.class})
    @ResponseBody
    public AjaxResp<Void> handlerOperationNotAllowed(OperationNotAllowed e) {
        return AjaxResp.error("驱动器（%s）不允许进行操作".formatted(e.getDriverName()));
    }
}
