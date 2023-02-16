package zdy.graduation.design.jdisk.module.directLink.controller;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import zdy.graduation.design.jdisk.core.service.SystemConfigService;
import zdy.graduation.design.jdisk.core.util.AjaxResp;
import zdy.graduation.design.jdisk.module.directLink.entity.DirectLink;
import zdy.graduation.design.jdisk.module.directLink.service.DirectLinkService;
import zdy.graduation.design.jdisk.module.virtualFileSystem.entity.VirtualDriver;
import zdy.graduation.design.jdisk.module.virtualFileSystem.service.DriverService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/direct/link")
public class DirectLinkManagerController {
    @Resource
    private DirectLinkService directLinkService;
    @Resource
    private DriverService driverService;
    @Resource
    private SystemConfigService systemConfigService;

    @RequestMapping("/list")
    @ResponseBody
    public AjaxResp<?> list() {
        List<DirectLink> list = directLinkService.list();
        return AjaxResp.success(list);
    }

    record GenerateRequest(List<String> paths, String driverKey) {
    }

    @PostMapping("/generate")
    @ResponseBody
    public AjaxResp<?> generate(@RequestBody GenerateRequest reqBody) {
        VirtualDriver driver = driverService.getDriver(reqBody.driverKey());
        if (driver == null) {
            return AjaxResp.error("驱动器%s不存在".formatted(reqBody.driverKey()));
        }

        String domain = systemConfigService.get("domain").getValue();
        int driverId = driver.getId();

        List<Map<String, String>> result = new ArrayList<>();
        for (String path : reqBody.paths) {
            DirectLink directLink = directLinkService.findLinkByPathAndDriverId(path, driverId);
            if (directLink == null) {
                directLink = directLinkService.generateDirectLink(path, driverId);
            }
            String shortLink = "%s/s/%s".formatted(domain, directLink.getKey());
            String pathLink = "%s/direct/link/%s/%s".formatted(domain, driver.getKey(), directLink.getPath());
            result.add(Map.of(
                    "shortLink", shortLink,
                    "pathLink", pathLink
            ));
        }
        return AjaxResp.success(result);
    }

    @PostMapping("/remove/{directKey}")
    @ResponseBody
    public AjaxResp<?> remove(@PathVariable String directKey) {
        boolean flag = directLinkService.remove(directKey);
        return flag ? AjaxResp.success() : AjaxResp.failure();
    }

}
