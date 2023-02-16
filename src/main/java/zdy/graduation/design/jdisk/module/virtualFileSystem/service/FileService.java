package zdy.graduation.design.jdisk.module.virtualFileSystem.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.teasoft.bee.osql.Op;
import org.teasoft.bee.osql.SuidRich;
import org.teasoft.honey.osql.core.BeeFactoryHelper;
import org.teasoft.honey.osql.core.ConditionImpl;
import zdy.graduation.design.jdisk.module.virtualFileSystem.entity.VirtualDriver;
import zdy.graduation.design.jdisk.module.virtualFileSystem.entity.VirtualFile;
import zdy.graduation.design.jdisk.module.virtualFileSystem.exception.OperationNotAllowed;
import zdy.graduation.design.jdisk.module.virtualFileSystem.util.VirtualPath;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class FileService {
    private final Logger logger = LoggerFactory.getLogger(FileService.class);
    private final SuidRich suid = BeeFactoryHelper.getSuidRich();

    public boolean addFile(MultipartFile uploadFile,
                           String fileName,
                           String parent,
                           VirtualDriver driver) throws IOException, OperationNotAllowed {
        if (!driver.getEnableFileOperator()) {
            throw new OperationNotAllowed(driver.getName());
        }

        //驱动器路径+文件所在的目录路径+文件名
        Path path = Paths.get(driver.getPath(), parent, fileName);
        //将上传的文件放到指定位置
        uploadFile.transferTo(path);

        File file = path.toFile();
        VirtualFile virtualFile = new VirtualFile();
        virtualFile.setPath(VirtualPath.get(parent, fileName));
        virtualFile.setDriverId(driver.getId());
        virtualFile.setName(fileName);
        virtualFile.setType("F");
        virtualFile.setParent(parent);
        virtualFile.setModified(new Date(file.lastModified()));
        virtualFile.setSize(file.length());

        int n = suid.insert(virtualFile);
        logger.info("驱动器({})添加文件{}{}", driver.getName(), virtualFile.getPath(), n > 0 ? "成功" : "失败");
        return n > 0;
    }

    public boolean addDirectory(String directoryName, String parent, VirtualDriver driver)
            throws IOException, OperationNotAllowed {
        if (!driver.getEnableFileOperator()) {
            throw new OperationNotAllowed(driver.getName());
        }

        //驱动器路径+文件所在的目录路径+文件名
        Path path = Paths.get(driver.getPath(), parent, directoryName);
        Files.createDirectories(path);

        File directory = path.toFile();
        VirtualFile virtualFile = new VirtualFile();
        virtualFile.setPath(VirtualPath.get(parent, directoryName));
        virtualFile.setDriverId(driver.getId());
        virtualFile.setName(directoryName);
        virtualFile.setType("D");
        virtualFile.setParent(parent);
        virtualFile.setModified(new Date(directory.lastModified()));

        int n = suid.insert(virtualFile);
        logger.info("驱动器({})添加目录{}{}", driver.getName(), virtualFile.getPath(), n > 0 ? "成功" : "失败");
        return n > 0;
    }

    public boolean updateFile(String oldVirtualPath,
                              String newName,
                              String newParent,
                              VirtualDriver driver) throws IOException, OperationNotAllowed {
        if (!driver.getEnableFileOperator()) {
            throw new OperationNotAllowed(driver.getName());
        }

        int driverId = driver.getId();

        VirtualFile entity = new VirtualFile();
        entity.setPath(oldVirtualPath);
        entity.setDriverId(driverId);
        List<VirtualFile> selectList = suid.select(entity);
        if (selectList.isEmpty()) {
            throw new FileNotFoundException("数据库中找不到该文件");
        }
        VirtualFile virtualFile = selectList.get(0);
        boolean isDir = Objects.equals("D", virtualFile.getType());

        Path currentPath = Paths.get(driver.getPath(), oldVirtualPath);
        Path newPath = Paths.get(driver.getPath(), newParent, newName);

        if (Files.notExists(currentPath)) {
            throw new FileNotFoundException("%s不存在".formatted(currentPath));
        }
        Files.createDirectories(newPath.getParent());

        File file = Files.move(currentPath, newPath, StandardCopyOption.REPLACE_EXISTING).toFile();
        VirtualFile newVirtualFile = new VirtualFile();
        newVirtualFile.setPath(VirtualPath.get(newParent, newName));
        newVirtualFile.setDriverId(driver.getId());
        newVirtualFile.setName(newName);
        newVirtualFile.setType(virtualFile.getType());
        newVirtualFile.setParent(newParent);
        newVirtualFile.setModified(new Date(file.lastModified()));
        newVirtualFile.setSize(isDir ? null : file.length());

        if (isDir) {
            List<VirtualFile> childList = suid.select(
                    new VirtualFile(),
                    new ConditionImpl().op("parent", Op.like, virtualFile.getPath() + "%")
            );
            for (VirtualFile childVirtualFile : childList) {
                VirtualFile newChildVirtualFile = new VirtualFile();
                String newChildParent = childVirtualFile.getParent()
                        .replace(virtualFile.getPath(), newVirtualFile.getPath());
                newChildVirtualFile.setPath(VirtualPath.get(newChildParent, childVirtualFile.getName()));
                newChildVirtualFile.setDriverId(childVirtualFile.getDriverId());
                newChildVirtualFile.setName(childVirtualFile.getName());
                newChildVirtualFile.setType(childVirtualFile.getType());
                newChildVirtualFile.setParent(newChildParent);
                newChildVirtualFile.setModified(childVirtualFile.getModified());
                newChildVirtualFile.setSize(childVirtualFile.getSize());

                if (suid.update(childVirtualFile, newChildVirtualFile) < 0) {
                    logger.warn("驱动器({})更新{}{}为{}失败",
                            driver.getName(),
                            Objects.equals("D", childVirtualFile.getType()) ? "目录" : "文件",
                            childVirtualFile.getPath(),
                            newChildVirtualFile.getPath());
                }
            }
        }

        int n = suid.update(virtualFile, newVirtualFile);
        logger.info("驱动器({})更新{}{}为{}{}",
                driver.getName(),
                isDir ? "目录" : "文件",
                virtualFile.getPath(),
                newVirtualFile.getPath(),
                n > 0 ? "成功" : "失败");

        return n > 0;
    }

    public boolean removeFile(String virtualPath, VirtualDriver driver) throws IOException, OperationNotAllowed {
        if (!driver.getEnableFileOperator()) {
            throw new OperationNotAllowed(driver.getName());
        }

        int driverId = driver.getId();

        VirtualFile entity = new VirtualFile();
        entity.setPath(virtualPath);
        entity.setDriverId(driverId);
        List<VirtualFile> selectList = suid.select(entity);
        if (selectList.isEmpty()) {
            throw new FileNotFoundException("数据库中找不到该文件");
        }
        VirtualFile virtualFile = selectList.get(0);
        boolean isDir = Objects.equals("D", virtualFile.getType());

        int n = 0;
        if (isDir) {
            n = suid.delete(new VirtualFile(),
                    new ConditionImpl().op("parent", Op.like, virtualFile.getPath() + "%")
            );
        }
        int nn = suid.delete(virtualFile);
        logger.info("驱动器({})删除{}{}{}",
                driver.getName(),
                isDir ? "目录" : "文件",
                virtualFile.getPath(),
                nn > 0 ? "成功" : "失败");
        if (nn > 0) n++;
        logger.info("驱动器({})共删除{}个记录", driver.getName(), n);

        Path path = Paths.get(driver.getPath(), virtualPath);
        if (Files.notExists(path)) {
            throw new FileNotFoundException("%s不存在".formatted(path));
        }
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                Files.delete(path);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                if (e != null) {
                    throw e;
                }
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
        return nn > 0;
    }

    public List<VirtualFile> getFileInDir(String dir, VirtualDriver driver) {
        if (dir == null) {
            dir = "/";
        }
        if (!dir.startsWith("/")) {
            dir = "/" + dir;
        }

        VirtualFile entity = new VirtualFile();
        entity.setParent(dir);
        entity.setDriverId(driver.getId());

        return suid.select(entity);
    }

    public Resource getFileResource(String virtualPath, VirtualDriver driver) {
        if (driver == null) {
            return null;
        }
        Path path = Paths.get(driver.getPath(), virtualPath);
        if (Files.notExists(path)) {
            return null;
        }
        if (!Files.isRegularFile(path)) {
            return null;
        }
        return new FileSystemResource(path);
    }
}
