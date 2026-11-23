package com.whenmo.controller.web;

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

    @GetMapping("/card/{cdbId}/{cardId}")
    public String cardPage(@PathVariable Integer cdbId, @PathVariable Integer cardId, Model model) {
        model.addAttribute("cdbId", cdbId);
        model.addAttribute("cardId", cardId);
        return "view/card";
    }

    @GetMapping("/decks")
    public String publicDecksPage() {
        return "view/decks";
    }

    @GetMapping("/deck/public/{deckId}")
    public String publicDeckPage(@PathVariable Integer deckId, Model model) {
        model.addAttribute("mode", "public");
        model.addAttribute("deckId", deckId);
        return "view/deck";
    }

    @GetMapping("/cdbs")
    public String showCdbsPage() {
        return "view/cdbs";
    }

    @GetMapping("/boards")
    public String showBoardsPage() {
        return "view/boards";
    }
}