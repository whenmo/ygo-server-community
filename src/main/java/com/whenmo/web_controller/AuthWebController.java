package com.whenmo.web_controller;

import com.whenmo.config.SecurityFilterProperties;
import com.whenmo.model.database.request.UserLoginRequest;
import com.whenmo.model.database.response.UserResponse;
import com.whenmo.service.database.UserRestService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.UUID;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthWebController {

    private final UserRestService userRestService;
    private final SecurityFilterProperties securityProps;

    // ============================================================
    // Login
    // ============================================================

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(required = false) String redirect, Model model) {
        model.addAttribute("redirect", redirect);
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam(required = false) String name,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String redirect,
            HttpServletResponse response,
            HttpServletRequest request,
            Model model) {

        model.addAttribute("name", name);
        model.addAttribute("redirect", redirect);

        try {
            UserLoginRequest loginReq = UserLoginRequest.builder().name(name).password(password).build();
            UserResponse loggedIn = userRestService.login(loginReq);
            String credential = UUID.randomUUID().toString();

            // 建立 Cookie
            boolean isSecure = request.isSecure();
            ResponseCookie cred = ResponseCookie.from(securityProps.getCookieName(), credential)
                    .httpOnly(true)
                    .secure(isSecure)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(Duration.ofHours(2))
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cred.toString());

            // 建立 Session
            HttpSession session = request.getSession(true);
            session.setAttribute(securityProps.getSessionKey(), credential);
            session.setAttribute("userId", loggedIn.getId());
            session.setAttribute("isAdmin", Boolean.TRUE.equals(loggedIn.getIsAdmin()));

            // 重導向處理
            if (redirect != null && !redirect.isBlank()) {
                return "redirect:" + redirect;
            }
            return "redirect:/";

        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("msg", e.getMessage());
            model.addAttribute("success", false);
            return "auth/login";
        }
    }
    // ============================================================
    // Register
    // ============================================================

    @GetMapping("/register")
    public String showRegisterForm(@RequestParam(required = false) String redirect, Model model) {
        model.addAttribute("redirect", redirect);
        return "auth/register";
    }

    // ============================================================
    // Logout
    // ============================================================

    @RequestMapping(path = "/logout", method = { RequestMethod.GET, RequestMethod.POST })
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        boolean isSecure = request.isSecure();
        ResponseCookie expired = ResponseCookie.from(securityProps.getCookieName(), "")
                .httpOnly(true)
                .secure(isSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, expired.toString());

        return "redirect:" + securityProps.getLoginPath();
    }
}