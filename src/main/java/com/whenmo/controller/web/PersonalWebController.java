package com.whenmo.controller.web;

import com.whenmo.controller.helper.SecurityHelper;
import com.whenmo.controller.helper.SecurityHelper.RequestAuth;
import com.whenmo.model.database.response.UserResponse;
import com.whenmo.service.database.UserRestService;

import jakarta.servlet.http.HttpServletRequest;
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

    private final SecurityHelper securityHelper;
    private final UserRestService userService;

    @GetMapping("/profile")
    public String profile(HttpServletRequest httpRequest, Model model) {
        RequestAuth requestAuth = securityHelper.auth(httpRequest);
        String redirectPath = requestAuth.resolveRedirect(false);
        if (redirectPath != null)
            return redirectPath;

        UserResponse user = userService.getUserById(requestAuth.getUserId());

        model.addAttribute("user", user);
        return "personal/profile";
    }

    @GetMapping("/decks")
    public String decks(HttpServletRequest httpRequest, Model model) {
        RequestAuth requestAuth = securityHelper.auth(httpRequest);
        String redirectPath = requestAuth.resolveRedirect(false);
        if (redirectPath != null)
            return redirectPath;

        model.addAttribute("userId", requestAuth.getUserId());
        return "personal/decks";
    }

    @GetMapping("/decks/create")
    public String createDeckPage(HttpServletRequest httpRequest, Model model) {
        RequestAuth requestAuth = securityHelper.auth(httpRequest);
        String redirectPath = requestAuth.resolveRedirect(false);
        if (redirectPath != null)
            return redirectPath;

        model.addAttribute("userId", requestAuth.getUserId());
        model.addAttribute("deckId", null);
        return "personal/deck-edit";
    }

    @GetMapping("/decks/{deckId}/edit")
    public String editDeck(HttpServletRequest httpRequest, @PathVariable Integer deckId, Model model) {
        RequestAuth requestAuth = securityHelper.auth(httpRequest);
        String redirectPath = requestAuth.resolveRedirect(false);
        if (redirectPath != null)
            return redirectPath;

        model.addAttribute("userId", requestAuth.getUserId());
        model.addAttribute("deckId", deckId);
        return "personal/deck-edit";
    }

    @GetMapping("/decks/{deckId}/view")
    public String mineDeckPage(HttpServletRequest httpRequest, @PathVariable Integer deckId, Model model) {
        RequestAuth requestAuth = securityHelper.auth(httpRequest);
        String redirectPath = requestAuth.resolveRedirect(false);
        if (redirectPath != null)
            return redirectPath;

        model.addAttribute("mode", "private");
        model.addAttribute("deckId", deckId);
        return "view/deck";
    }
}