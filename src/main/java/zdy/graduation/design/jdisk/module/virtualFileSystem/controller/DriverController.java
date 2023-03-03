package zdy.graduation.design.jdisk.module.virtualFileSystem.controller;

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
    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @RequestMapping("/list")
    @ResponseBody
    public AjaxResp<?> list() {
        List<VirtualDriver> list = driverService.getAllDriver();
        return AjaxResp.success(list);
    }
}
