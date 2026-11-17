package com.whenmo.controller.web;

import com.whenmo.controller.helper.SecurityHelper;
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
        Integer userId = securityHelper.getAuthUserId(httpRequest);
        if (userId == null)
            return securityHelper.getRedirectPath("/personal/profile");

        model.addAttribute("profileName", userService.getUserById(userId).getName());
        return "personal/profile";
    }

    @GetMapping("/decks")
    public String decks(HttpServletRequest httpRequest, Model model) {
        Integer userId = securityHelper.getAuthUserId(httpRequest);
        if (userId == null)
            return securityHelper.getRedirectPath("/personal/decks");

        model.addAttribute("userId", userId);
        return "personal/decks";
    }

    @GetMapping("/decks/create")
    public String createDeckPage(HttpServletRequest httpRequest, Model model) {
        Integer userId = securityHelper.getAuthUserId(httpRequest);
        if (userId == null)
            return securityHelper.getRedirectPath("/personal/decks/create");

        model.addAttribute("userId", userId);
        model.addAttribute("deckId", null);
        return "personal/deck-edit";
    }

    @GetMapping("/decks/{deckId}/edit")
    public String editDeck(HttpServletRequest httpRequest, @PathVariable Integer deckId, Model model) {
        Integer userId = securityHelper.getAuthUserId(httpRequest);
        if (userId == null)
            return securityHelper.getRedirectPath("/personal/decks/" + deckId + "/edit");

        model.addAttribute("userId", userId);
        model.addAttribute("deckId", deckId);
        return "personal/deck-edit";
    }
}