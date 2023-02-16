package zdy.graduation.design.jdisk.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.teasoft.bee.osql.SuidRich;
import org.teasoft.honey.osql.core.BeeFactoryHelper;
import zdy.graduation.design.jdisk.core.entity.SystemConfig;

import java.util.List;

@Service
public class SystemConfigService {
    private final Logger logger = LoggerFactory.getLogger(SystemConfigService.class);
    private final SuidRich suid = BeeFactoryHelper.getSuidRich();

    public void init() {
        SystemConfig installed = suid.selectOne(new SystemConfig("installed"));

        if (installed != null) {
            return;
        }

        SystemConfig[] configs = {
                new SystemConfig("installed", String.valueOf(false), "是否已初始化安装"),
                new SystemConfig("siteName", "JDisk", "站点名称"),
                new SystemConfig("username", null, "管理员账号"),
                new SystemConfig("password", null, "管理员密码"),
                new SystemConfig("domain", "http://127.0.0.1:8080", "站点域名"),
                new SystemConfig("avatar", null, "头像地址"),
                new SystemConfig("customVideoSuffix", "mp4,webm,m3u8,rmvb,avi,swf,3gp,mkv,flv,mov", "自定义视频文件后缀格式"),
                new SystemConfig("customImageSuffix", "gif,jpg,jpeg,png,bmp,webp,ico", "自定义图像文件后缀格式"),
                new SystemConfig("customAudioSuffix", "mp3,wav,wma,ogg,aac,flac,m4a", "自定义音频文件后缀格式"),
                new SystemConfig("customTextSuffix", "scss,sass,kt,gitignore,bat,properties,yml,css,js,md,xml,txt,py,go,html,less,php,rb,rust,script,java,sh,sql", "自定义文本文件后缀格式"),
                new SystemConfig("rootShowStorage", String.valueOf(false), "根目录是否显示所有存储源"),
                new SystemConfig("maxFileUploads", "3", "最大同时上传文件数")
        };

        suid.insert(configs);
        logger.info("初始化系统配置完成");
    }

    public boolean update(String key, String value) {
        SystemConfig old = get(key);
        if (old == null) {
            return false;
        }
        SystemConfig updateEntity = new SystemConfig(old.getId(), key, value, old.getRemark());
        int n = suid.update(old, updateEntity);
        logger.info("更新系统配置{}为{}{}", key, value, n > 0 ? "成功" : "失败");
        return n > 0;
    }

    public SystemConfig get(String key) {
        return suid.selectOne(new SystemConfig(key));
    }

    public List<SystemConfig> getAll() {
        return suid.select(new SystemConfig());
    }
}
