package zdy.graduation.design.jdisk.core.service;

import org.springframework.stereotype.Service;
import org.teasoft.bee.osql.BeeSql;
import org.teasoft.honey.osql.core.BeeFactory;

@Service
public class SQLTableService {
    private final BeeSql beeSql = BeeFactory.getHoneyFactory().getBeeSql();

    public void initTables() {
        beeSql.modify(getCreateSystemConfigSQL());
        beeSql.modify(getCreateVirtualDriverSQL());
        beeSql.modify(getCreateVirtualFileSQL());
        beeSql.modify(getCreateDirectLinkSQL());
    }

    private String getCreateSystemConfigSQL() {
        return """
                CREATE TABLE IF NOT EXISTS `system_config`  (
                  `id` int NOT NULL AUTO_INCREMENT,
                  `key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                  `value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                  PRIMARY KEY (`id`) USING BTREE,
                  UNIQUE INDEX `key`(`key`) USING BTREE
                ) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;
                """;
    }

    private String getCreateVirtualDriverSQL() {
        return """
                CREATE TABLE IF NOT EXISTS `virtual_driver`  (
                  `id` int NOT NULL AUTO_INCREMENT,
                  `enable` tinyint(1) NOT NULL COMMENT '是否启用',
                  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '虚拟驱动器名',
                  `key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '虚拟驱动器代号，用于url',
                  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '虚拟驱动器对应真实文件系统的路径',
                  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '备注',
                  `is_private` tinyint(1) NOT NULL COMMENT '是否生成签名下载链接',
                  `token_time` int NOT NULL COMMENT '下载链接签名存活时间',
                  `enable_file_operator` tinyint(1) NOT NULL COMMENT '是否允许文件操作',
                  PRIMARY KEY (`id`) USING BTREE,
                  UNIQUE INDEX `key`(`key`) USING BTREE
                ) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;
                """;
    }

    private String getCreateVirtualFileSQL() {
        return """
                CREATE TABLE IF NOT EXISTS `virtual_file`  (
                  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件的路径',
                  `driver_id` int NOT NULL COMMENT '文件所在的驱动器ID',
                  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件名',
                  `type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'F代表文件，D代表目录',
                  `parent` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件所在的目录',
                  `modified` datetime NOT NULL COMMENT '文件的最后修改日期',
                  `size` bigint NULL DEFAULT NULL COMMENT '文件的大小，目录的大小为null',
                  PRIMARY KEY (`path`, `driver_id`) USING BTREE,
                  INDEX `driver`(`driver_id`) USING BTREE,
                  CONSTRAINT `driver` FOREIGN KEY (`driver_id`) REFERENCES `virtual_driver` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
                ) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;
                """;
    }

    private String getCreateDirectLinkSQL() {
        return """
                CREATE TABLE IF NOT EXISTS `direct_link`  (
                  `id` int NOT NULL AUTO_INCREMENT,
                  `key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '直链的key',
                  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '直链对应文件的路径',
                  `driver_id` int NOT NULL COMMENT '对应驱动器的id',
                  `create_date` datetime NOT NULL COMMENT '直链创建时间',
                  PRIMARY KEY (`id`) USING BTREE,
                  UNIQUE INDEX `key`(`key`) USING BTREE,
                  INDEX `driver_id`(`driver_id`) USING BTREE,
                  INDEX `file`(`path`, `driver_id`) USING BTREE,
                  CONSTRAINT `driver_id` FOREIGN KEY (`driver_id`) REFERENCES `virtual_driver` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
                  CONSTRAINT `file` FOREIGN KEY (`path`, `driver_id`) REFERENCES `virtual_file` (`path`, `driver_id`) ON DELETE CASCADE ON UPDATE CASCADE
                ) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;
                """;
    }
}
