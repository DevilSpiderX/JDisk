package zdy.graduation.design.jdisk.module.admin.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class AdminInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
        HttpSession session = req.getSession();
        if (isAdmin(session)) {
            return true;
        }
        req.getRequestDispatcher("/error/notAdmin").forward(req, resp);
        return false;
    }

    public static boolean isAdmin(HttpSession session) {
        return Optional.ofNullable((Boolean) session.getAttribute("admin")).orElse(false);
    }
}
