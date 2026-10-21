package com.whenmo.web_controller;

import com.whenmo.config.SecurityFilterProperties;
import com.whenmo.service.database.UserRestService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/personal")
@RequiredArgsConstructor
public class PersonalWebController {

    private final SecurityFilterProperties securityProps;
    private final UserRestService userService;

    @GetMapping("/profile")
    public String profile(HttpServletRequest request, Model model) {
        Integer userId = getAuthenticatedUserId(request);
        if (userId == null) {
            return "redirect:" + securityProps.getLoginPath() + "?redirect=/personal/profile";
        }

        model.addAttribute("profileName", userService.getUserById(userId).getName());
        return "personal/profile";
    }

    @GetMapping("/decks")
    public String decks(HttpServletRequest request) {
        if (getAuthenticatedUserId(request) == null) {
            return "redirect:" + securityProps.getLoginPath() + "?redirect=/personal/decks";
        }
        return "personal/deck";
    }

    @GetMapping("/decks/{deckId}/edit")
    public String editDeck(@PathVariable Integer deckId, HttpServletRequest request, Model model) {
        if (getAuthenticatedUserId(request) == null) {
            return "redirect:" + securityProps.getLoginPath() + "?redirect=/personal/decks/" + deckId + "/edit";
        }
        model.addAttribute("deckId", deckId);
        return "personal/deck-edit";
    }

    private Integer getAuthenticatedUserId(HttpServletRequest request) {
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
}