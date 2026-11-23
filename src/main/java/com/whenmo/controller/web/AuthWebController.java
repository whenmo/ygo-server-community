package com.whenmo.controller.web;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;
import java.util.UUID;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthWebController {

    private static final Duration SESSION_MAX_AGE = Duration.ofHours(2);

    private final UserRestService userRestService;
    private final SecurityFilterProperties securityProps;

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(required = false) String redirect, Model model) {
        model.addAttribute("redirect", redirect);
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String redirect,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {
        model.addAttribute("name", name);
        model.addAttribute("redirect", redirect);

        try {
            UserLoginRequest loginReq = UserLoginRequest.builder().name(name).password(password).build();
            UserResponse loggedIn = userRestService.login(loginReq);

            String credential = UUID.randomUUID().toString();
            addAuthCookie(response, credential, request.isSecure(), SESSION_MAX_AGE);

            HttpSession session = request.getSession(true);
            session.setAttribute(securityProps.getSessionKey(), credential);
            session.setAttribute("userId", loggedIn.getId());
            session.setAttribute("isAdmin", Boolean.TRUE.equals(loggedIn.getIsAdmin()));

            return "redirect:" + resolveTargetRedirect(redirect);
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("msg", e.getMessage());
            model.addAttribute("success", false);
            return "auth/login";
        }
    }

    @GetMapping("/register")
    public String showRegisterForm(@RequestParam(required = false) String redirect, Model model) {
        model.addAttribute("redirect", redirect);
        return "auth/register";
    }

    @RequestMapping(path = "/logout", method = { RequestMethod.GET, RequestMethod.POST })
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        addAuthCookie(response, "", request.isSecure(), Duration.ZERO);
        return "redirect:" + securityProps.getLoginPath();
    }

    private void addAuthCookie(HttpServletResponse response, String value, boolean secure, Duration maxAge) {
        ResponseCookie cookie = ResponseCookie.from(securityProps.getCookieName(), value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String resolveTargetRedirect(String redirect) {
        if (redirect == null || redirect.isBlank()) {
            return "/";
        }
        String trimmed = redirect.trim();
        if (!trimmed.startsWith("/") || trimmed.startsWith("//")) {
            return "/";
        }
        return trimmed;
    }
}