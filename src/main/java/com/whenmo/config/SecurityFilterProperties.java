package com.whenmo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "security.filter")
public class SecurityFilterProperties {

    /** 正面列舉：需要進行登入驗證的受保護路徑（含子路徑）。 */
    private List<String> protectedPaths = new ArrayList<>();

    /** 登入憑證 Cookie 名稱（前端持有的那一支）。 */
    private String cookieName = ".cred";

    /** 後端 Session 中存放相對憑證的鍵值。 */
    private String sessionKey = "AUTH_CRED";

    /** 驗證失敗時導向的登入頁路徑。 */
    private String loginPath = "/auth/login";

    public List<String> getProtectedPaths() {
        return protectedPaths;
    }

    public void setProtectedPaths(List<String> protectedPaths) {
        this.protectedPaths = protectedPaths;
    }

    public String getCookieName() {
        return cookieName;
    }

    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public String getLoginPath() {
        return loginPath;
    }

    public void setLoginPath(String loginPath) {
        this.loginPath = loginPath;
    }
}
