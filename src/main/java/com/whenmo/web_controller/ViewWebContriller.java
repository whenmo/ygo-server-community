package com.whenmo.web_controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/view")
@RequiredArgsConstructor
public class ViewWebContriller {

    @GetMapping("/cards")
    public String showLoginForm(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("keyword", keyword);
        return "view/cards";
    }

    @GetMapping("/card/{configId}/{cardId}")
    public String cardPage(@PathVariable Long configId, @PathVariable Long cardId, Model model) {
        model.addAttribute("configId", configId);
        model.addAttribute("cardId", cardId);
        return "view/card";
    }

    @GetMapping("/decks")
    public String publicDecksPage() {
        return "view/decks";
    }

    @GetMapping("/deck/{userId}/{deckId}")
    public String deckPage(@PathVariable Integer userId, @PathVariable Integer deckId, Model model) {
        model.addAttribute("userId", userId);
        model.addAttribute("deckId", deckId);
        return "view/deck";
    }
}