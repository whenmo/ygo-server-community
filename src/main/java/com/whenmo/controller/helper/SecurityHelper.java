package com.whenmo.controller.helper;

import com.whenmo.config.SecurityFilterProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityHelper {

    private final SecurityFilterProperties securityProps;

    /**
     * 取得重定向路徑
     */
    public String getRedirectPath(String redirectPath) {
        return "redirect:" + securityProps.getLoginPath() + "?redirect=" + redirectPath;
    }

    /**
     * 取得目前登入使用者的 ID
     */
    public Integer getAuthUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null)
            return null;

        Object userId = session.getAttribute("userId");
        Object sessionCredential = session.getAttribute(securityProps.getSessionKey());
        if (!(userId instanceof Integer) || sessionCredential == null)
            return null;

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (securityProps.getCookieName().equals(cookie.getName())
                        && sessionCredential.toString().equals(cookie.getValue())) {
                    return (Integer) userId;
                }
            }
        }
        return null;
    }

    /**
     * 檢查目前登入使用者是否為管理員（直接從 Session 讀取）
     */
    public boolean isAuthAdmin(HttpServletRequest request) {
        Integer userId = getAuthUserId(request);
        if (userId == null) {
            return false;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        Object isAdmin = session.getAttribute("isAdmin");
        return Boolean.TRUE.equals(isAdmin);
    }
}