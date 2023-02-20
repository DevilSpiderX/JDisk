package zdy.graduation.design.jdisk.module.directLink.controller;

import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import zdy.graduation.design.jdisk.core.util.AjaxResp;
import zdy.graduation.design.jdisk.module.directLink.entity.DirectLink;
import zdy.graduation.design.jdisk.module.directLink.exception.DirectKeyNotFound;
import zdy.graduation.design.jdisk.module.directLink.service.DirectLinkService;
import zdy.graduation.design.jdisk.module.virtualFileSystem.entity.VirtualDriver;
import zdy.graduation.design.jdisk.module.virtualFileSystem.service.DriverService;
import zdy.graduation.design.jdisk.module.virtualFileSystem.service.FileService;

@Controller
public class DirectLinkController {
    @Resource
    private DirectLinkService directLinkService;
    @Resource
    private DriverService driverService;
    @Resource
    private FileService fileService;

    @RequestMapping("/s/{directKey}")
    public String shortLink(@PathVariable String directKey) throws DirectKeyNotFound {
        DirectLink directLink = directLinkService.findLinkByKey(directKey);
        if (directLink == null) {
            throw new DirectKeyNotFound(directKey);
        }

        VirtualDriver driver = driverService.getDriver(directLink.getDriverId());

        return "redirect:/direct/link/%s%s".formatted(directLinkService.URLEncode(driver.getKey()),
                directLinkService.encodePath(directLink.getPath()));
    }

    @RequestMapping("/direct/link/{driverKey}/{*path}")
    public ResponseEntity<?> pathLink(@PathVariable String driverKey, @PathVariable String path) {
        VirtualDriver driver = driverService.getDriver(driverKey);
        if (driver == null) {
            return ResponseEntity.ok(AjaxResp.error("驱动器%s不存在".formatted(driverKey)));
        }

        DirectLink directLink = directLinkService.findLinkByPathAndDriverId(path, driver.getId());
        if (directLink == null) {
            return ResponseEntity.ok(AjaxResp.error("%s%s未生成直链，无法下载".formatted(driver.getKey(), path)));
        }

        org.springframework.core.io.Resource resource = fileService.getFileResource(path, driver);
        if (resource == null) {
            return ResponseEntity.ok(AjaxResp.failure("文件不存在"));
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
