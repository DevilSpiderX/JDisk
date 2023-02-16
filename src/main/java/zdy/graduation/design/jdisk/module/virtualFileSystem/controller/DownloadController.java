package zdy.graduation.design.jdisk.module.virtualFileSystem.controller;

import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import zdy.graduation.design.jdisk.core.util.AjaxResp;
import zdy.graduation.design.jdisk.module.virtualFileSystem.entity.VirtualDriver;
import zdy.graduation.design.jdisk.module.virtualFileSystem.service.DriverService;
import zdy.graduation.design.jdisk.module.virtualFileSystem.service.FileService;
import zdy.graduation.design.jdisk.module.signature.service.SignatureService;

@Controller
@RequestMapping("/dl/{driverKey}")
public class DownloadController {
    @Resource
    private DriverService driverService;
    @Resource
    private FileService fileService;
    @Resource
    private SignatureService signatureService;

    @RequestMapping("/{*path}")
    public ResponseEntity<?> download(@PathVariable String driverKey,
                                      @PathVariable String path,
                                      @RequestParam(value = "signature", required = false) String sign) {
        VirtualDriver driver = driverService.getDriver(driverKey);
        if (driver == null) {
            return ResponseEntity.ok(AjaxResp.error("驱动器%s不存在".formatted(driverKey)));
        }
        if (driver.getIsPrivate()) {
            if (sign == null) {
                return ResponseEntity.ok(AjaxResp.error("签名不存在"));
            }
            if (!signatureService.check(sign)) {
                return ResponseEntity.ok(AjaxResp.failure("无效签名"));
            }
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
