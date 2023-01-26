package zdy.graduation.design.jdisk.core.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import zdy.graduation.design.jdisk.core.exception.NotAdminException;
import zdy.graduation.design.jdisk.core.util.AjaxResp;

import java.util.Map;

@Controller
@RequestMapping("/error")
public class HttpErrorController implements ErrorController {

    @GetMapping("/404")
    public ModelAndView error404() {
        return new ModelAndView("forward:/", HttpStatus.OK);
    }

    @RequestMapping("/{status:\\d+}")
    @ResponseBody
    public AjaxResp<?> error_request(@PathVariable int status) {
        return AjaxResp.of(status, HttpStatus.valueOf(status).getReasonPhrase())
                .setData(Map.of("timestamp", System.currentTimeMillis()));
    }

    @RequestMapping("/notAdmin")
    public void NoAdmin() throws NotAdminException {
        throw new NotAdminException();
    }
}
