package com.whenmo.web_controller;

import com.whenmo.config.SecurityFilterProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminSetWebController {

    private final SecurityFilterProperties securityProps;

    /**
     * 管理員設定首頁（包含 CDB 數據庫配置管理）
     * 訪問路徑：GET /admin/cdb
     */
    @GetMapping("/cdb")
    public String cdbSettingPage(HttpServletRequest request, Model model) {
        String redirectPath = redirectCheck(request, model, "cdb");
        if (redirectPath != null)
            return redirectPath;

        return "admin/cdb";
    }

    /**
     * 用戶配置管理頁面
     * 訪問路徑：GET /admin/user
     */
    @GetMapping("/user")
    public String userSettingPage(HttpServletRequest request, Model model) {
        String redirectPath = redirectCheck(request, model, "user");
        if (redirectPath != null)
            return redirectPath;

        return "admin/user";
    }

    /**
     * 作者配置管理頁面
     * 訪問路徑：GET /admin/diyer
     */
    @GetMapping("/diyer")
    public String diyerSettingPage(HttpServletRequest request, Model model) {
        String redirectPath = redirectCheck(request, model, "diyer");
        if (redirectPath != null)
            return redirectPath;

        return "admin/diyer";
    }

    private String redirectCheck(HttpServletRequest request, Model model, String redirect) {
        HttpSession session = request.getSession(false);

        // 驗證是否已登入
        if (session == null || session.getAttribute(securityProps.getSessionKey()) == null) {
            return "redirect:" + securityProps.getLoginPath() + "?redirect=/admin/" + redirect;
        }

        // 驗證是否為管理員
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (!Boolean.TRUE.equals(isAdmin)) {
            return "redirect:/";
        }

        String currentUser = (String) session.getAttribute(securityProps.getSessionKey());

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAdmin", true);

        return null;
    }
}