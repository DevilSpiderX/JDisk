package zdy.graduation.design.jdisk.module.virtualFileSystem.controller;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import zdy.graduation.design.jdisk.core.util.AjaxResp;
import zdy.graduation.design.jdisk.module.virtualFileSystem.entity.VirtualDriver;
import zdy.graduation.design.jdisk.module.virtualFileSystem.entity.VirtualFile;
import zdy.graduation.design.jdisk.module.virtualFileSystem.service.DriverService;
import zdy.graduation.design.jdisk.module.virtualFileSystem.service.FileService;

import java.util.List;

@Controller
@RequestMapping("/api/file")
public class FileController {
    @Resource
    private DriverService driverService;
    @Resource
    private FileService fileService;

    @RequestMapping("/list/{driverKey}/{*dir}")
    @ResponseBody
    public AjaxResp<?> list(@PathVariable String driverKey, @PathVariable String dir) {
        VirtualDriver driver = driverService.getDriver(driverKey);
        if (driver == null) {
            return AjaxResp.error("驱动器%s不存在".formatted(driverKey));
        }

        List<VirtualFile> list = fileService.getFileInDir(dir, driver);
        return AjaxResp.success(list);
    }
}
