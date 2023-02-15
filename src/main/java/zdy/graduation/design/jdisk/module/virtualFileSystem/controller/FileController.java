package zdy.graduation.design.jdisk.module.virtualFileSystem.controller;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import zdy.graduation.design.jdisk.core.util.AjaxResp;
import zdy.graduation.design.jdisk.module.virtualFileSystem.entity.VirtualFile;
import zdy.graduation.design.jdisk.module.virtualFileSystem.exception.OperationNotAllowed;
import zdy.graduation.design.jdisk.module.virtualFileSystem.service.FileService;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/api/file")
public class FileController {
    private final Logger logger = LoggerFactory.getLogger(FileController.class);
    @Resource(name = "fileService")
    private FileService fileService;

    @RequestMapping("/list")
    @ResponseBody
    public AjaxResp<?> list(@RequestParam("dir") String dir, @RequestParam("id") int id) {
        List<VirtualFile> list = fileService.getFileInDir(dir, id);
        return AjaxResp.success(list).setDataCount((long) list.size());
    }

    record UpdateRequest(String path,
                         String newName,
                         String newParent,
                         int driverId) {
    }

    @PostMapping("/update")
    @ResponseBody
    public AjaxResp<?> update(@RequestBody UpdateRequest reqBody) throws OperationNotAllowed {
        try {
            boolean flag = fileService.updateFile(reqBody.path(),
                    reqBody.newName(),
                    reqBody.newParent(),
                    reqBody.driverId());
            return flag ? AjaxResp.success() : AjaxResp.failure();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return AjaxResp.error(e.getMessage());
        }
    }

    record RemoveRequest(String path, int driverId) {
    }

    @PostMapping("/remove")
    @ResponseBody
    public AjaxResp<?> remove(@RequestBody RemoveRequest reqBody) throws OperationNotAllowed {
        try {
            boolean flag = fileService.removeFile(reqBody.path(), reqBody.driverId());
            return flag ? AjaxResp.success() : AjaxResp.failure();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return AjaxResp.error(e.getMessage());
        }
    }

    record MkdirRequest(String name, String parent, int driverId) {
    }

    @PostMapping("/mkdir")
    @ResponseBody
    public AjaxResp<?> mkdir(@RequestBody MkdirRequest reqBody) throws OperationNotAllowed {
        try {
            boolean flag = fileService.addDirectory(reqBody.name(), reqBody.parent(), reqBody.driverId());
            return flag ? AjaxResp.success() : AjaxResp.failure();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return AjaxResp.error(e.getMessage());
        }
    }

}
