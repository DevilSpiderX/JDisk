package zdy.graduation.design.jdisk.module.signature.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import zdy.graduation.design.jdisk.core.util.MyCipher;
import zdy.graduation.design.jdisk.module.virtualFileSystem.entity.VirtualDriver;

import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SignatureService {
    private final Logger logger = LoggerFactory.getLogger(SignatureService.class);
    private final Map<String, Date> signAndEndDate = new ConcurrentHashMap<>();

    public boolean check(String sign) {
        Date now = new Date();
        signAndEndDate.forEach((signature, date) -> {
            if (now.compareTo(date) > 0) {
                signAndEndDate.remove(signature);
            }
        });

        for (String signature : signAndEndDate.keySet()) {
            if (Objects.equals(sign, signature)) {
                return true;
            }
        }
        return false;
    }

    public String apply(String path, VirtualDriver driver) {
        if (!driver.getIsPrivate()) {
            return "";
        }
        try {
            String sign = MyCipher.bytes2Hex(MyCipher.SHA256(path + "DevilSpiderX"));
            Date endDate = new Date(System.currentTimeMillis() + driver.getTokenTime() * 1000);
            signAndEndDate.put(sign, endDate);
            return sign;
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
}
