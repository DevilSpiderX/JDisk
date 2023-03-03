package zdy.graduation.design.jdisk.module.virtualFileSystem.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import zdy.graduation.design.jdisk.core.util.AjaxResp;
import zdy.graduation.design.jdisk.module.virtualFileSystem.entity.VirtualDriver;
import zdy.graduation.design.jdisk.module.virtualFileSystem.exception.OperationNotAllowed;
import zdy.graduation.design.jdisk.module.virtualFileSystem.service.DriverService;
import zdy.graduation.design.jdisk.module.virtualFileSystem.service.FileService;

import java.io.IOException;

@Controller
@RequestMapping("/api/file/operate")
public class FileOpController {
    private final Logger logger = LoggerFactory.getLogger(FileOpController.class);
    private final DriverService driverService;
    private final FileService fileService;

    public FileOpController(DriverService driverService, FileService fileService) {
        this.driverService = driverService;
        this.fileService = fileService;
    }

    record UpdateRequest(String path,
                         String newName,
                         String newParent,
                         String driverKey) {
    }

    @PostMapping("/update")
    @ResponseBody
    public AjaxResp<?> update(@RequestBody UpdateRequest reqBody) throws OperationNotAllowed {
        VirtualDriver driver = driverService.getDriver(reqBody.driverKey());
        if (driver == null) {
            return AjaxResp.error("驱动器%s不存在".formatted(reqBody.driverKey()));
        }

        try {
            boolean flag = fileService.updateFile(reqBody.path(),
                    reqBody.newName(),
                    reqBody.newParent(),
                    driver);
            return flag ? AjaxResp.success() : AjaxResp.failure();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return AjaxResp.error(e.getMessage());
        }
    }

    record RemoveRequest(String path, String driverKey) {
    }

    @PostMapping("/remove")
    @ResponseBody
    public AjaxResp<?> remove(@RequestBody RemoveRequest reqBody) throws OperationNotAllowed {
        VirtualDriver driver = driverService.getDriver(reqBody.driverKey());
        if (driver == null) {
            return AjaxResp.error("驱动器%s不存在".formatted(reqBody.driverKey()));
        }

        try {
            boolean flag = fileService.removeFile(reqBody.path(), driver);
            return flag ? AjaxResp.success() : AjaxResp.failure();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return AjaxResp.error(e.getMessage());
        }
    }

    record MkdirRequest(String name, String parent, String driverKey) {
    }

    @PostMapping("/mkdir")
    @ResponseBody
    public AjaxResp<?> mkdir(@RequestBody MkdirRequest reqBody) throws OperationNotAllowed {
        VirtualDriver driver = driverService.getDriver(reqBody.driverKey());
        if (driver == null) {
            return AjaxResp.error("驱动器%s不存在".formatted(reqBody.driverKey()));
        }

        try {
            boolean flag = fileService.addDirectory(reqBody.name(), reqBody.parent(), driver);
            return flag ? AjaxResp.success() : AjaxResp.failure();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return AjaxResp.error(e.getMessage());
        }
    }

    @PostMapping("/upload/{driverKey}/{*parent}")
    @ResponseBody
    public AjaxResp<?> upload(@PathVariable String driverKey,
                              @PathVariable String parent,
                              @RequestParam("file") MultipartFile file,
                              @RequestParam("fileName") String fileName) throws OperationNotAllowed {
        VirtualDriver driver = driverService.getDriver(driverKey);
        if (driver == null) {
            return AjaxResp.error("驱动器%s不存在".formatted(driverKey));
        }

        try {
            boolean flag = fileService.addFile(file, fileName, parent, driver);
            return flag ? AjaxResp.success() : AjaxResp.failure();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return AjaxResp.error(e.getMessage());
        }
    }

}
