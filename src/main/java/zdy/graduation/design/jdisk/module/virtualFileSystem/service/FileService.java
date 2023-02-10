package zdy.graduation.design.jdisk.module.virtualFileSystem.service;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.teasoft.bee.osql.IncludeType;
import org.teasoft.bee.osql.Op;
import org.teasoft.bee.osql.SuidRich;
import org.teasoft.honey.osql.core.BeeFactoryHelper;
import org.teasoft.honey.osql.core.ConditionImpl;
import zdy.graduation.design.jdisk.module.virtualFileSystem.entity.VirtualDriver;
import zdy.graduation.design.jdisk.module.virtualFileSystem.entity.VirtualFile;
import zdy.graduation.design.jdisk.module.virtualFileSystem.exception.DriverExistedException;
import zdy.graduation.design.jdisk.module.virtualFileSystem.exception.DriverPathIsFileException;
import zdy.graduation.design.jdisk.module.virtualFileSystem.exception.DriverScanning;
import zdy.graduation.design.jdisk.module.virtualFileSystem.util.FileTree;
import zdy.graduation.design.jdisk.module.virtualFileSystem.util.VirtualPath;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service("fileService")
public class FileService {
    private final Logger logger = LoggerFactory.getLogger(FileService.class);
    private final SuidRich suid = BeeFactoryHelper.getSuidRich();

    private void checkPath(String path) throws DriverPathIsFileException {
        Path PATH = Paths.get(path);
        try {
            Files.createDirectories(PATH);
        } catch (FileAlreadyExistsException e) {
            throw new DriverPathIsFileException();
        } catch (IOException e) {
            logger.error("创建驱动器目录%s出现IO错误".formatted(path));
            logger.error(e.getMessage(), e);
        }
    }

    public boolean addDriver(String name,
                             String key,
                             String path,
                             String remark,
                             boolean isPrivate,
                             int tokenTime,
                             boolean enableFileOperator,
                             boolean enableFileAnnoOperator)
            throws DriverExistedException, DriverPathIsFileException {
        VirtualDriver driver = new VirtualDriver();
        driver.setKey(key);
        if (suid.exist(driver)) {
            throw new DriverExistedException(key);
        }
        checkPath(path);
        driver.setEnable(true);
        driver.setName(name);
        driver.setPath(path);
        driver.setRemark(remark);
        driver.setIsPrivate(isPrivate);
        driver.setTokenTime(tokenTime);
        driver.setEnableFileOperator(enableFileOperator);
        driver.setEnableFileAnnoOperator(enableFileAnnoOperator);
        int n = suid.insert(driver, IncludeType.INCLUDE_EMPTY);
        logger.info("创建驱动器{}", n > 0 ? "成功" : "失败");
        return n > 0;
    }

    public boolean removeDriver(int id) {
        int n = suid.deleteById(VirtualDriver.class, id);
        logger.info("删除驱动器{}{}", id, n > 0 ? "成功" : "失败");
        return n > 0;
    }

    public boolean updateDriver(int id,
                                boolean enable,
                                String name,
                                String key,
                                String path,
                                String remark,
                                boolean isPrivate,
                                int tokenTime,
                                boolean enableFileOperator,
                                boolean enableFileAnnoOperator)
            throws DriverPathIsFileException {
        checkPath(path);
        VirtualDriver driver = new VirtualDriver();
        driver.setId(id);
        driver.setEnable(enable);
        driver.setName(name);
        driver.setKey(key);
        driver.setPath(path);
        driver.setRemark(remark);
        driver.setIsPrivate(isPrivate);
        driver.setTokenTime(tokenTime);
        driver.setEnableFileOperator(enableFileOperator);
        driver.setEnableFileAnnoOperator(enableFileAnnoOperator);
        int n = suid.updateById(driver, new ConditionImpl().setIncludeType(IncludeType.INCLUDE_EMPTY));
        logger.info("修改驱动器{}{}", id, n > 0 ? "成功" : "失败");
        return n > 0;
    }

    public VirtualDriver getDriver(int id) {
        return suid.selectById(new VirtualDriver(), id);
    }

    public VirtualDriver getDriver(String key) {
        VirtualDriver driver = new VirtualDriver();
        driver.setKey(key);
        List<VirtualDriver> list = suid.select(driver, 1);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public List<VirtualDriver> getAllDriver() {
        return suid.select(new VirtualDriver());
    }

    private static final Map<Integer, Boolean> scanning = new ConcurrentHashMap<>();

    public void scanDriver(int id) throws DriverScanning, InterruptedException {
        VirtualDriver driver = getDriver(id);
        if (driver == null) return;
        if (scanning.getOrDefault(id, false)) {
            throw new DriverScanning(id);
        }
        scanning.put(id, true);
        FileTree root = FileTree.scanDriver(driver);
        int n = suid.delete(new VirtualFile(), new ConditionImpl().op("driver_id", Op.equal, driver.getId()));
        Thread scanThread = new Thread(() -> {
            if (n >= 0) {
                int count = addTreeToSQL(root);
                logger.info("驱动器{}扫描结束，共记录{}个文件", id, count);
            }
            scanning.put(id, false);
        });
        scanThread.setDaemon(false);
        scanThread.start();
        scanThread.join();
    }

    private int addTreeToSQL(@NotNull FileTree node) {
        List<FileTree> children = node.getChildren();
        if (children == null) return 0;
        int n = 0;
        List<VirtualFile> insertList = new ArrayList<>();
        for (FileTree child : children) {
            insertList.add(child.getFile());
            n += addTreeToSQL(child);
        }
        return n + suid.insert(insertList, IncludeType.INCLUDE_BOTH);
    }

    public boolean addFile(MultipartFile uploadFile,
                           String fileName,
                           String parent,
                           int driverId) throws IOException {
        VirtualDriver driver = getDriver(driverId);
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
        logger.info("驱动器{}添加文件{}{}", driverId, virtualFile.getPath(), n > 0 ? "成功" : "失败");
        return n > 0;
    }

    public boolean updateFile(String oldVirtualPath,
                              String newName,
                              String newParent,
                              int driverId) throws IOException {
        VirtualDriver driver = getDriver(driverId);
        VirtualFile virtualFile = new VirtualFile();
        virtualFile.setPath(oldVirtualPath);
        virtualFile.setDriverId(driverId);

        List<VirtualFile> selectList = suid.select(virtualFile);
        if (selectList.isEmpty()) {
            throw new FileNotFoundException();
        }
        virtualFile = selectList.get(0);
        //如果是目录的话，使用另一个方法
        if (Objects.equals("D", virtualFile.getType())) {
            return updateDirectory(newName, newParent, virtualFile, driver);
        }

        Path currentPath = Paths.get(driver.getPath(), oldVirtualPath);
        Path newPath = Paths.get(driver.getPath(), newParent, newName);

        if (Files.notExists(currentPath)) {
            throw new FileNotFoundException();
        }

        File file = Files.move(currentPath, newPath, StandardCopyOption.REPLACE_EXISTING).toFile();
        VirtualFile newVirtualFile = new VirtualFile();
        newVirtualFile.setPath(VirtualPath.get(newParent, newName));
        newVirtualFile.setDriverId(driver.getId());
        newVirtualFile.setName(newName);
        newVirtualFile.setType("F");
        newVirtualFile.setParent(newParent);
        newVirtualFile.setModified(new Date(file.lastModified()));
        newVirtualFile.setSize(file.length());

        int n = suid.update(virtualFile, newVirtualFile);
        logger.info("驱动器{}更新文件{}为{}{}",
                driverId,
                virtualFile.getPath(),
                newVirtualFile.getPath(),
                n > 0 ? "成功" : "失败");
        return n > 0;
    }

    private boolean updateDirectory(String newName,
                                    String newParent,
                                    VirtualFile virtualFile,
                                    VirtualDriver driver) {
        return false;
    }
}
