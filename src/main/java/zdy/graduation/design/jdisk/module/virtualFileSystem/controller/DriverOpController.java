package zdy.graduation.design.jdisk.module.virtualFileSystem.controller;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import zdy.graduation.design.jdisk.core.util.AjaxResp;
import zdy.graduation.design.jdisk.module.virtualFileSystem.exception.DriverExistedException;
import zdy.graduation.design.jdisk.module.virtualFileSystem.exception.DriverPathIsFileException;
import zdy.graduation.design.jdisk.module.virtualFileSystem.exception.DriverScanning;
import zdy.graduation.design.jdisk.module.virtualFileSystem.service.DriverService;

import java.util.Map;

@Controller
@RequestMapping("/api/driver/operate")
public class DriverOpController {
    private final Logger logger = LoggerFactory.getLogger(DriverOpController.class);
    @Resource
    private DriverService driverService;

    record AddRequest(String name,
                      String key,
                      String path,
                      String remark,
                      boolean isPrivate,
                      int tokenTime,
                      boolean enableFileOperator) {
    }

    @PostMapping("/add")
    @ResponseBody
    public AjaxResp<?> add(@RequestBody AddRequest reqBody)
            throws DriverExistedException, DriverPathIsFileException {
        int driverId = driverService.addDriver(
                reqBody.name(),
                reqBody.key(),
                reqBody.path(),
                reqBody.remark(),
                reqBody.isPrivate(),
                reqBody.tokenTime(),
                reqBody.enableFileOperator()
        );
        return driverId != -1 ? AjaxResp.success(Map.of("driverId", driverId)) : AjaxResp.failure();
    }

    @PostMapping("/remove")
    @ResponseBody
    public AjaxResp<?> remove(@RequestParam("id") int id) {
        boolean flag = driverService.removeDriver(id);
        return flag ? AjaxResp.success() : AjaxResp.failure();
    }

    record UpdateRequest(int id,
                         boolean enable,
                         String name,
                         String key,
                         String path,
                         String remark,
                         boolean isPrivate,
                         int tokenTime,
                         boolean enableFileOperator) {
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
                reqBody.enableFileOperator()
        );
        return flag ? AjaxResp.success() : AjaxResp.failure();
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
