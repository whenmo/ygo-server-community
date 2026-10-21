package com.whenmo.web_controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeWebController {

    /**
     * 當使用者訪問根路徑時，自動 HTTP 302 重導向至 /view/search
     */
    @GetMapping("/")
    public String index() {
        return "redirect:/view/cards";
    }
}