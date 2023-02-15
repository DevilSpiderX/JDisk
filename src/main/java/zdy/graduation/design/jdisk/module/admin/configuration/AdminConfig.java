package zdy.graduation.design.jdisk.module.admin.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import zdy.graduation.design.jdisk.module.admin.interceptor.AdminInterceptor;

@Configuration
public class AdminConfig implements WebMvcConfigurer {
    private final AdminInterceptor adminInterceptor;

    public AdminConfig(AdminInterceptor adminInterceptor) {
        this.adminInterceptor = adminInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminInterceptor)
                .order(0)
                .addPathPatterns(
                        "/api/admin/**",
                        "/api/system/config/update",
                        "/api/driver/**",
                        "/api/file/**"
                ).excludePathPatterns(
                        "/api/admin/login",
                        "/api/driver/list",
                        "/api/file/list"
                );
    }
}
