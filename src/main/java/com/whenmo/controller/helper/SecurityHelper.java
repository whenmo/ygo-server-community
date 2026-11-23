package com.whenmo.controller.helper;

import com.whenmo.config.SecurityFilterProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityHelper {

    private final SecurityFilterProperties securityProps;

    /**
     * 以單次 HttpServletRequest 初始化的驗證上下文：
     */
    public class RequestAuth {
        @Getter
        private final Integer userId;
        @Getter
        private final boolean isAuth;
        @Getter
        private final boolean isAdmin;
        @Getter
        private final String url;

        public RequestAuth(HttpServletRequest request) {
            Integer resolvedUserId = null;
            boolean resolvedAdmin = false;
            String resolvedUrl = "/";

            if (request != null) {
                HttpSession session = request.getSession(false);
                if (session != null) {
                    Object rawUserId = session.getAttribute("userId");
                    Object sessionCredential = session.getAttribute(securityProps.getSessionKey());
                    Cookie[] cookies = request.getCookies();

                    if (rawUserId instanceof Integer && sessionCredential != null && cookies != null) {
                        String expectedValue = sessionCredential.toString();
                        String expectedName = securityProps.getCookieName();

                        for (Cookie cookie : cookies) {
                            if (expectedName != null
                                    && expectedName.equals(cookie.getName())
                                    && expectedValue.equals(cookie.getValue())) {
                                resolvedUserId = (Integer) rawUserId;
                                resolvedAdmin = Boolean.TRUE.equals(session.getAttribute("isAdmin"));
                                break;
                            }
                        }
                    }
                }

                String uri = request.getRequestURI();
                String query = request.getQueryString();
                if (uri != null && !uri.isBlank()) {
                    resolvedUrl = (query != null && !query.isBlank()) ? uri + "?" + query : uri;
                }
            }

            this.userId = resolvedUserId;
            this.isAuth = resolvedUserId != null;
            this.isAdmin = this.isAuth && resolvedAdmin;
            this.url = resolvedUrl;
        }

        /**
         * 根據登入與權限狀態取得重定向路徑(自動為原址)，皆符合則回傳 null
         */
        public String resolveRedirect(boolean requireAdmin) {
            return resolveRedirect(url, requireAdmin);
        }

        /**
         * 根據登入與權限狀態取得 HttpStatus，皆符合則回傳 null
         */
        public HttpStatus resolveHttpStatus(boolean requireAdmin) {
            if (!isAuth)
                return HttpStatus.UNAUTHORIZED;
            else if (requireAdmin && !isAdmin)
                return HttpStatus.FORBIDDEN;
            return null;
        }

        /**
         * 根據登入與權限狀態取得重定向路徑，皆符合則回傳 null
         */
        public String resolveRedirect(String redirectPath, boolean requireAdmin) {
            if (!isAuth)
                return getLoginRedirect(redirectPath);
            else if (requireAdmin && !isAdmin)
                return "redirect:/";
            return null;
        }

        /**
         * 傳回重定向到登入頁面的 redirectPath
         */
        public String getLoginRedirect(String redirectPath) {
            String target = (redirectPath == null || redirectPath.isBlank()) ? "/" : redirectPath;
            String loginPath = securityProps.getLoginPath();
            String separator = loginPath.contains("?") ? "&" : "?";
            return "redirect:" + loginPath + separator + "redirect=" + target;
        }
    }

    /**
     * 建立以目前 HttpServletRequest 初始化的驗證物件
     */
    public RequestAuth auth(HttpServletRequest request) {
        return new RequestAuth(request);
    }
}