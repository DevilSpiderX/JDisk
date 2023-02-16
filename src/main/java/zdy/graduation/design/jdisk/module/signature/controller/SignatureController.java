package zdy.graduation.design.jdisk.module.signature.controller;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import zdy.graduation.design.jdisk.core.util.AjaxResp;
import zdy.graduation.design.jdisk.module.signature.service.SignatureService;
import zdy.graduation.design.jdisk.module.virtualFileSystem.entity.VirtualDriver;
import zdy.graduation.design.jdisk.module.virtualFileSystem.service.DriverService;

@Controller
@RequestMapping("/api/signature")
public class SignatureController {
    @Resource
    private DriverService driverService;
    @Resource
    private SignatureService signatureService;

    record ApplyRequest(String path, String driverKey) {
    }

    @PostMapping("/apply")
    @ResponseBody
    public AjaxResp<?> apply(@RequestBody ApplyRequest reqBody) {
        VirtualDriver driver = driverService.getDriver(reqBody.driverKey());
        if (driver == null) {
            return AjaxResp.error("驱动器%s不存在".formatted(reqBody.driverKey()));
        }

        String sign = signatureService.apply(reqBody.path(), driver);
        if (sign == null) {
            return AjaxResp.error();
        }
        return AjaxResp.success(sign);
    }
}
