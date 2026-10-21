package com.whenmo.filter;

import com.whenmo.config.SecurityFilterProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 登入雙重驗證 Filter（請求進入 Controller 前的安全閘門）
 *
 * 繼承 Spring 的 OncePerRequestFilter，保證同一次請求只執行一次驗證
 * （避免 forward / dispatch 造成重複攔截）。
 *
 * 驗證流程：
 * 1. 取出本次請求路徑（去除 context path）。
 * 2. 比對「正面列舉」的受保護路徑（含子路徑）—— 不在清單者直接放行。
 * 3. 命中受保護路徑 → 執行雙重驗證：
 * ① 前端 Cookie（.cred）存在且有值
 * ② 後端 Session 存在對應憑證
 * ③ 兩者「值必須相等」
 * 三者皆成立才放行；任一不成立即導回登入頁。
 *
 * 為何要雙重驗證？
 * 單靠 Cookie 容易被竄改／重放；單靠 Session 又無法綁定特定瀏覽器憑證。
 * 兩者交叉比對後，攻擊者必須同時握有合法 Cookie 與伺服器端對應的 Session 才能通過。
 */
public class AuthenticationFilter extends OncePerRequestFilter {

    private final SecurityFilterProperties props;

    // 透過建構式注入組態（由 FilterConfig 建立 Bean 時傳入），維持不可變、利於測試
    public AuthenticationFilter(SecurityFilterProperties props) {
        this.props = props;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 取得「相對於應用程式」的路徑：去掉 context path，才能與設定檔的 /orders 等對齊
        String path = request.getRequestURI().substring(request.getContextPath().length());

        // 步驟 2：不在正面列舉（保護清單）內 → 公開資源，直接放行
        if (!isProtected(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 步驟 3：受保護路徑 → 執行雙重驗證
        if (isAuthenticated(request)) {
            filterChain.doFilter(request, response); // 通過，交棒給後續 Filter / Controller
        } else {
            // 檢查是否為前端 API / AJAX 請求
            String requestedWith = request.getHeader("X-Requested-With");
            String accept = request.getHeader("Accept");
            boolean isApiRequest = "XMLHttpRequest".equals(requestedWith)
                    || (accept != null && accept.contains("application/json"));

            if (isApiRequest) {
                // 回傳 401 狀態碼與 JSON，讓前端 JS 自行處理跳轉
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter()
                        .write("{\"error\":\"Unauthorized\",\"loginPath\":\"" + props.getLoginPath() + "\"}");
            } else {
                // 一般網頁請求：導回登入頁，並帶上原始目標位址
                response.sendRedirect(request.getContextPath() + props.getLoginPath() + "?redirect=" + path);
            }
        }
    }

    /**
     * 正面列舉比對（含子路徑）。
     * 規則：請求路徑等於某受保護前綴，或以「前綴 + /」開頭即視為命中。
     * 例：保護 /orders → 命中 /orders、/orders/、/orders/123；但不會誤判 /orders-report。
     */
    private boolean isProtected(String path) {
        for (String prefix : props.getProtectedPaths()) {
            if (prefix == null || prefix.isBlank()) {
                continue;
            }
            if (path.equals(prefix) || path.startsWith(prefix + "/")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 雙重驗證核心：Cookie 憑證必須與後端 Session 憑證同時存在且相等。
     */
    private boolean isAuthenticated(HttpServletRequest request) {
        // ① 取出前端 .cred Cookie 的值
        String cookieCred = readCookie(request, props.getCookieName());
        if (cookieCred == null || cookieCred.isBlank()) {
            return false; // 沒有 Cookie 憑證，免談
        }

        // ② 取出後端 Session 的相對憑證（false：不主動建立新 Session）
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false; // 伺服器端沒有任何登入狀態
        }
        Object sessionCred = session.getAttribute(props.getSessionKey());
        if (sessionCred == null) {
            return false; // Session 內找不到對應憑證
        }

        // ③ 雙重比對：兩端憑證必須相等
        return cookieCred.equals(sessionCred.toString());
    }

    /**
     * 從請求中讀取指定名稱的 Cookie 值，找不到回傳 null。
     */
    private String readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
