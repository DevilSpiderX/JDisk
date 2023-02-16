package zdy.graduation.design.jdisk.module.directLink.exception.handler;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import zdy.graduation.design.jdisk.core.util.AjaxResp;
import zdy.graduation.design.jdisk.module.directLink.exception.DirectKeyNotFound;

@ControllerAdvice
public class DirectLinkExceptionHandler {
    @ExceptionHandler({DirectKeyNotFound.class})
    @ResponseBody
    public AjaxResp<?> handlerDirectKeyNotFound(DirectKeyNotFound e) {
        return AjaxResp.error("直链key:%s不存在".formatted(e.getKey()));
    }
}
