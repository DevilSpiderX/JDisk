package zdy.graduation.design.jdisk.module.virtualFileSystem.exception.handler;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import zdy.graduation.design.jdisk.core.util.AjaxResp;
import zdy.graduation.design.jdisk.module.virtualFileSystem.exception.DriverExistedException;
import zdy.graduation.design.jdisk.module.virtualFileSystem.exception.DriverPathIsFileException;

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
}
