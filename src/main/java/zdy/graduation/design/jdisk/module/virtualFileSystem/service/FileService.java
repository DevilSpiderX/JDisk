package zdy.graduation.design.jdisk.module.virtualFileSystem.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.teasoft.bee.osql.IncludeType;
import org.teasoft.bee.osql.SuidRich;
import org.teasoft.honey.osql.core.BeeFactoryHelper;
import org.teasoft.honey.osql.core.ConditionImpl;
import zdy.graduation.design.jdisk.module.virtualFileSystem.entity.VirtualDriver;
import zdy.graduation.design.jdisk.module.virtualFileSystem.exception.DriverExistedException;

import java.util.List;

@Service("fileService")
public class FileService {
    private final Logger logger = LoggerFactory.getLogger(FileService.class);
    private final SuidRich suid = BeeFactoryHelper.getSuidRich();

    public boolean addDriver(String name,
                             String key,
                             String path,
                             String remark,
                             boolean isPrivate,
                             int tokenTime,
                             boolean enableFileOperator,
                             boolean enableFileAnnoOperator) throws DriverExistedException {
        VirtualDriver driver = new VirtualDriver();
        driver.setKey(key);
        if (suid.exist(driver)) {
            throw new DriverExistedException(key);
        }
        driver.setEnable(true);
        driver.setName(name);
        driver.setPath(path);
        driver.setRemark(remark);
        driver.setIsPrivate(isPrivate);
        driver.setTokenTime(tokenTime);
        driver.setEnableFileOperator(enableFileOperator);
        driver.setEnableFileAnnoOperator(enableFileAnnoOperator);
        int n = suid.insert(driver, IncludeType.INCLUDE_EMPTY);
        logger.info(n > 0 ? "创建驱动器成功" : "创建驱动器失败");
        return n > 0;
    }

    public boolean removeDriver(int id) {
        int n = suid.deleteById(VirtualDriver.class, id);
        logger.info((n > 0 ? "删除驱动器%d成功" : "删除驱动器%d失败").formatted(id));
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
                                boolean enableFileAnnoOperator) {
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
        logger.info((n > 0 ? "修改驱动器%d成功" : "修改驱动器%d失败").formatted(id));
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
}
