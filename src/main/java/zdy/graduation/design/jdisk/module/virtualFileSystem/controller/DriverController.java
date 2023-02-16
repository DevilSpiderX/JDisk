package zdy.graduation.design.jdisk.module.virtualFileSystem.controller;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import zdy.graduation.design.jdisk.core.util.AjaxResp;
import zdy.graduation.design.jdisk.module.virtualFileSystem.entity.VirtualDriver;
import zdy.graduation.design.jdisk.module.virtualFileSystem.service.DriverService;

import java.util.List;

@Controller
@RequestMapping("/api/driver")
public class DriverController {
    @Resource
    private DriverService driverService;

    @RequestMapping("/list")
    @ResponseBody
    public AjaxResp<?> list() {
        List<VirtualDriver> list = driverService.getAllDriver();
        return AjaxResp.success(list);
    }
}
