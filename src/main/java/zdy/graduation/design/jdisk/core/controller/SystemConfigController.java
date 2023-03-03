package zdy.graduation.design.jdisk.core.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import zdy.graduation.design.jdisk.core.entity.SystemConfig;
import zdy.graduation.design.jdisk.core.service.SystemConfigService;
import zdy.graduation.design.jdisk.core.util.AjaxResp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/api/system/config")
public class SystemConfigController {
    private final SystemConfigService systemConfigService;

    public SystemConfigController(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    @RequestMapping("/list")
    @ResponseBody
    public AjaxResp<?> list() {
        List<SystemConfig> list = systemConfigService.getAll();
        Map<String, Object> map = new HashMap<>(list.size());

        Pattern numberPattern = Pattern.compile("\\d+");
        Pattern booleanPattern = Pattern.compile("true|false");
        list.forEach(config -> {
            if (config.getKey().equals("password")) return;
            String value = config.getValue();
            if (value == null) return;

            if (numberPattern.matcher(value).matches()) {
                map.put(config.getKey(), Integer.parseInt(value));
            } else if (booleanPattern.matcher(value).matches()) {
                map.put(config.getKey(), Boolean.parseBoolean(value));
            } else {
                map.put(config.getKey(), value);
            }
        });

        return AjaxResp.success(map);
    }

    record SystemConfigRequest(String key, Object value) {
    }

    @PostMapping("/update")
    @ResponseBody
    public AjaxResp<?> update(@RequestBody SystemConfigRequest reqBody) {
        String key = reqBody.key();
        if (key == null) {
            return AjaxResp.error("key不能为null");
        }

        String value = null;
        if (reqBody.value() != null) {
            value = String.valueOf(reqBody.value());
        }
        boolean flag = systemConfigService.update(key, value);
        if (flag) {
            return AjaxResp.success();
        } else {
            return AjaxResp.failure();
        }
    }

    record InstallRequest(String siteName, String username, String password, String domain) {
    }

    @PostMapping("/install")
    @ResponseBody
    public AjaxResp<?> install(@RequestBody InstallRequest reqBody) {
        boolean flag = systemConfigService.install(reqBody.siteName(),
                reqBody.username(),
                reqBody.password(),
                reqBody.domain());
        return flag ? AjaxResp.success() : AjaxResp.failure();
    }
}
