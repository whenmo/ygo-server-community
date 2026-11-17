package com.whenmo.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/view")
@RequiredArgsConstructor
public class CdbWebContriller {

    @GetMapping("/cdbs")
    public String showCdbsPage() {
        return "view/cdbs";
    }

}