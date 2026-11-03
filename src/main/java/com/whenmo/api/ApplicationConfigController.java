package com.whenmo.api;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.whenmo.config.ApplicationConfig;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ApplicationConfigController {

    private final ApplicationConfig applicationConfig;

    @GetMapping
    public Map<String, String> getConfig() {
        return Map.of(
                "server_community_name", applicationConfig.getServerCommunityName(),
                "default_strings_api", applicationConfig.getDefaultStringsApi(),
                "fallback_unknown_card", applicationConfig.getFallbackUnknownCard(),
                "card_forbidden_url", applicationConfig.getCardForbiddenUrl(),
                "card_limit_url", applicationConfig.getCardLimitUrl(),
                "card_semi_limit_url", applicationConfig.getCardSemiLimitUrl());
    }
}
