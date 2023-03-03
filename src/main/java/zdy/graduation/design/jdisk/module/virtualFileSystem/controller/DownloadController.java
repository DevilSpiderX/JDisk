package zdy.graduation.design.jdisk.module.virtualFileSystem.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import zdy.graduation.design.jdisk.core.util.AjaxResp;
import zdy.graduation.design.jdisk.module.signature.service.SignatureService;
import zdy.graduation.design.jdisk.module.virtualFileSystem.entity.VirtualDriver;
import zdy.graduation.design.jdisk.module.virtualFileSystem.service.DriverService;
import zdy.graduation.design.jdisk.module.virtualFileSystem.service.FileService;

@Controller
@RequestMapping("/dl/{driverKey}")
public class DownloadController {
    private final DriverService driverService;
    private final FileService fileService;
    private final SignatureService signatureService;

    public DownloadController(DriverService driverService, FileService fileService, SignatureService signatureService) {
        this.driverService = driverService;
        this.fileService = fileService;
        this.signatureService = signatureService;
    }

    @RequestMapping("/{*path}")
    public ResponseEntity<?> download(@PathVariable String driverKey,
                                      @PathVariable String path,
                                      @RequestParam(value = "signature", required = false) String sign,
                                      @RequestParam(value = "type", defaultValue = "false", required = false)
                                      boolean isGetType) {
        final VirtualDriver driver = driverService.getDriver(driverKey);
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
        Resource resource = fileService.getFileResource(path, driver);
        if (resource == null) {
            return ResponseEntity.ok(AjaxResp.failure("文件不存在"));
        }
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (isGetType) {
            mediaType = MediaTypeFactory.getMediaType(resource).orElse(mediaType);
        }
        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(resource);
    }
}
