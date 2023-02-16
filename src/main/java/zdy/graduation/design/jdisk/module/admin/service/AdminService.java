package zdy.graduation.design.jdisk.module.admin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.teasoft.bee.osql.SuidRich;
import org.teasoft.honey.osql.core.BeeFactoryHelper;
import zdy.graduation.design.jdisk.core.entity.SystemConfig;

import java.util.Objects;

@Service
public class AdminService {
    private final Logger logger = LoggerFactory.getLogger(AdminService.class);
    private final SuidRich suid = BeeFactoryHelper.getSuidRich();

    public boolean verification(String username, String password) {
        SystemConfig usernameSC = suid.selectOne(new SystemConfig("username"));
        SystemConfig passwordSC = suid.selectOne(new SystemConfig("password"));

        return Objects.equals(username, usernameSC.getValue()) &&
                Objects.equals(password, passwordSC.getValue());
    }
}
