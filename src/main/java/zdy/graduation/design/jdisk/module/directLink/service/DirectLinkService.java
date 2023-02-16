package zdy.graduation.design.jdisk.module.directLink.service;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.teasoft.bee.osql.SuidRich;
import org.teasoft.honey.osql.core.BeeFactoryHelper;
import zdy.graduation.design.jdisk.module.directLink.entity.DirectLink;

import java.util.Date;
import java.util.List;
import java.util.Random;

@Service
public class DirectLinkService {
    private final Logger logger = LoggerFactory.getLogger(DirectLinkService.class);
    private final SuidRich suid = BeeFactoryHelper.getSuidRich();

    public DirectLink findLinkByPathAndDriverId(String path, int driverId) {
        return suid.selectOne(new DirectLink(path, driverId));
    }

    public DirectLink findLinkByKey(String key) {
        return suid.selectOne(new DirectLink(key));
    }

    public DirectLink generateDirectLink(String path, int driverId) {
        DirectLink directLink;
        String linkKey;
        do {
            linkKey = randomString(6);
            directLink = findLinkByKey(linkKey);
        } while (directLink != null);

        directLink = new DirectLink();
        directLink.setKey(linkKey);
        directLink.setPath(path);
        directLink.setDriverId(driverId);
        directLink.setCreateDate(new Date());

        int n = suid.insert(directLink);
        logger.info("创建直链({}){}", linkKey, n > 0 ? "成功" : "失败");
        return n > 0 ? directLink : null;
    }

    private @NotNull String randomString(int length) {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int n = random.nextInt(base.length());
            sb.append(base.charAt(n));
        }
        return sb.toString();
    }

    public boolean remove(String key) {
        int n = suid.delete(new DirectLink(key));
        logger.info("删除直链({}){}", key, n > 0 ? "成功" : "失败");
        return n > 0;
    }

    public List<DirectLink> list() {
        return suid.select(new DirectLink());
    }
}
