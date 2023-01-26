package zdy.graduation.design.jdisk.core.exception.handler;

import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import zdy.graduation.design.jdisk.core.exception.NotAdminException;
import zdy.graduation.design.jdisk.core.util.AjaxResp;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({NotAdminException.class})
    @ResponseBody
    public AjaxResp<Void> handleNotAdminException() {
        return AjaxResp.notAdmin();
    }

    @ExceptionHandler({ServletRequestBindingException.class})
    @ResponseBody
    public AjaxResp<Void> handleServletRequestBindingException(ServletRequestBindingException e) {
        return AjaxResp.error(e.getMessage());
    }
}
