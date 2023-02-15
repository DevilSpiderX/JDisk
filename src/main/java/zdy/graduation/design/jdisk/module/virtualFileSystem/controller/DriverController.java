package zdy.graduation.design.jdisk.module.virtualFileSystem.controller;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import zdy.graduation.design.jdisk.core.util.AjaxResp;
import zdy.graduation.design.jdisk.module.virtualFileSystem.entity.VirtualDriver;
import zdy.graduation.design.jdisk.module.virtualFileSystem.exception.DriverExistedException;
import zdy.graduation.design.jdisk.module.virtualFileSystem.exception.DriverPathIsFileException;
import zdy.graduation.design.jdisk.module.virtualFileSystem.exception.DriverScanning;
import zdy.graduation.design.jdisk.module.virtualFileSystem.service.DriverService;

import java.util.List;

@Controller
@RequestMapping("/api/driver")
public class DriverController {
    private final Logger logger = LoggerFactory.getLogger(DriverController.class);
    @Resource(name = "driverService")
    private DriverService driverService;

    @RequestMapping
    @ResponseBody
    public AjaxResp<?> get() {
        List<VirtualDriver> list = driverService.getAllDriver();
        return AjaxResp.success(list);
    }

    record AddRequest(String name,
                      String key,
                      String path,
                      String remark,
                      boolean isPrivate,
                      int tokenTime,
                      boolean enableFileOperator,
                      boolean enableFileAnnoOperator) {
    }

    @PostMapping("/add")
    @ResponseBody
    public AjaxResp<?> add(@RequestBody AddRequest reqBody)
            throws DriverExistedException, DriverPathIsFileException {
        boolean flag = driverService.addDriver(
                reqBody.name(),
                reqBody.key(),
                reqBody.path(),
                reqBody.remark(),
                reqBody.isPrivate(),
                reqBody.tokenTime(),
                reqBody.enableFileOperator(),
                reqBody.enableFileAnnoOperator()
        );
        if (flag) {
            return AjaxResp.success();
        } else {
            return AjaxResp.failure();
        }
    }

    @PostMapping("/remove")
    @ResponseBody
    public AjaxResp<?> remove(@RequestParam("id") int id) {
        boolean flag = driverService.removeDriver(id);
        if (flag) {
            return AjaxResp.success();
        } else {
            return AjaxResp.failure();
        }
    }

    record UpdateRequest(int id,
                         boolean enable,
                         String name,
                         String key,
                         String path,
                         String remark,
                         boolean isPrivate,
                         int tokenTime,
                         boolean enableFileOperator,
                         boolean enableFileAnnoOperator) {
    }

    @PostMapping("/update")
    @ResponseBody
    public AjaxResp<?> update(@RequestBody UpdateRequest reqBody) throws DriverPathIsFileException {
        boolean flag = driverService.updateDriver(
                reqBody.id(),
                reqBody.enable(),
                reqBody.name(),
                reqBody.key(),
                reqBody.path(),
                reqBody.remark(),
                reqBody.isPrivate(),
                reqBody.tokenTime(),
                reqBody.enableFileOperator(),
                reqBody.enableFileAnnoOperator()
        );
        if (flag) {
            return AjaxResp.success();
        } else {
            return AjaxResp.failure();
        }
    }

    @PostMapping("/scan")
    @ResponseBody
    public AjaxResp<?> scan(@RequestParam("id") int id) {
        try {
            driverService.scanDriver(id);
            return AjaxResp.success();
        } catch (DriverScanning e) {
            return AjaxResp.failure("驱动器%d正在扫描，请扫描完毕后再试".formatted(id));
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
            return AjaxResp.error("扫描过程被打断");
        }
    }
}
