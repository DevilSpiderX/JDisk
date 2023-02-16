package zdy.graduation.design.jdisk.module.admin.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import zdy.graduation.design.jdisk.core.util.AjaxResp;
import zdy.graduation.design.jdisk.module.admin.service.AdminService;

import java.util.Map;

@Controller
@RequestMapping("/api/admin")
public class AdminController {
    private final Logger logger = LoggerFactory.getLogger(AdminController.class);
    @Resource
    private AdminService adminService;

    record LoginRequest(String username, String password) {
    }

    @PostMapping("/login")
    public ResponseEntity<AjaxResp<?>> login(@RequestBody LoginRequest reqBody, HttpServletRequest req) {
        if (reqBody.username() == null || reqBody.password() == null) {
            return ResponseEntity.ok(AjaxResp.error());
        }

        String username = reqBody.username();
        String password = reqBody.password();

        if (adminService.verification(username, password)) {
            HttpHeaders headers = new HttpHeaders();

            int SESSION_MAX_AGE = 2592000;

            HttpSession session = req.getSession();
            session.setMaxInactiveInterval(SESSION_MAX_AGE);
            session.setAttribute("admin", true);

            ResponseCookie cookie = ResponseCookie.from("JSESSIONID", session.getId())
                    .maxAge(SESSION_MAX_AGE)
                    .path("/")
                    .httpOnly(true)
                    .secure(req.isSecure())
                    .build();
            headers.add(HttpHeaders.SET_COOKIE, cookie.toString());

            logger.info("管理员{}登录成功", username);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(AjaxResp.success(Map.of(
                            "username", username
                    )));
        } else {
            return ResponseEntity.ok(AjaxResp.failure("用户名密码错误"));
        }
    }
}
