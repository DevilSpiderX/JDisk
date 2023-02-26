package zdy.graduation.design.jdisk.core.configuration;

import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import zdy.graduation.design.jdisk.core.service.SQLTableService;
import zdy.graduation.design.jdisk.core.service.SystemConfigService;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;

@Component
public class JDiskRunner implements ApplicationRunner {
    private final Logger logger = LoggerFactory.getLogger(JDiskRunner.class);
    @Resource
    private SQLTableService sqlTableService;
    @Resource
    private SystemConfigService systemConfigService;

    @Override
    public void run(ApplicationArguments args) {
        try (FileWriter writer = new FileWriter("jdisk.pid")) {
            writer.write(String.valueOf(ManagementFactory.getRuntimeMXBean().getPid()));
            writer.write(System.lineSeparator());
            writer.flush();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        sqlTableService.initTables();
        systemConfigService.init();
    }

    @PreDestroy
    public void destroy() {
        logger.info("关闭服务器");
    }
}
