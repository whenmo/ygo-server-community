package com.whenmo.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

@Getter
@Service
public class ApplicationConfig {

    private static final String REMOTE_CONFIG_URL = "https://raw.githubusercontent.com/whenmo/ygo-server-community/main/server_community/src/main/resources/static/config.json";
    private static final String DEFAULT_COMMUNITY_NAME = "YGO社區";
    private static final String DEFAULT_STRINGS_API = "https://raw.githubusercontent.com/Fluorohydride/ygopro/master/strings.conf";
    private static final String DEFAULT_FALLBACK_UNKNOWN_CARD = "https://cdn.jsdelivr.net/gh/whenmo/ygo-server-community/src/main/resources/static/image/unknown_card.jpg";
    private static final String DEFAULT_CARD_FORBIDDEN_URL = "https://cdn.jsdelivr.net/gh/whenmo/ygo-server-community/src/main/resources/static/image/limit/limit_0.png";
    private static final String DEFAULT_CARD_LIMIT_URL = "https://cdn.jsdelivr.net/gh/whenmo/ygo-server-community/src/main/resources/static/image/limit/limit_1.png";
    private static final String DEFAULT_CARD_SEMI_LIMIT_URL = "https://cdn.jsdelivr.net/gh/whenmo/ygo-server-community/src/main/resources/static/image/limit/limit_2.png";

    private final ObjectMapper objectMapper = new ObjectMapper();
    // The editable config is beside the server_community project directory.
    private final Path rootConfigPath = Path.of("..").resolve("config.json").toAbsolutePath().normalize();

    private String serverCommunityName = DEFAULT_COMMUNITY_NAME;
    private String defaultStringsApi = DEFAULT_STRINGS_API;
    private String fallbackUnknownCard = DEFAULT_FALLBACK_UNKNOWN_CARD;
    private String cardForbiddenUrl = DEFAULT_CARD_FORBIDDEN_URL;
    private String cardLimitUrl = DEFAULT_CARD_LIMIT_URL;
    private String cardSemiLimitUrl = DEFAULT_CARD_SEMI_LIMIT_URL;

    @PostConstruct
    public synchronized void load() {
        byte[] config = readRootConfig();
        if (config == null) {
            config = readRemoteConfig();
        }
        if (config == null) {
            config = readBundledConfig();
        }
        if (config == null) {
            config = bundledDefaults();
        }

        if (!apply(config)) {
            config = bundledDefaults();
            apply(config);
        }
        writeRootConfig(config);
    }

    private byte[] readRootConfig() {
        try {
            return Files.isRegularFile(rootConfigPath) ? Files.readAllBytes(rootConfigPath) : null;
        } catch (IOException ignored) {
            return null;
        }
    }

    private byte[] readRemoteConfig() {
        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
            HttpRequest request = HttpRequest.newBuilder(URI.create(REMOTE_CONFIG_URL))
                    .timeout(Duration.ofSeconds(10)).GET().build();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            return response.statusCode() >= 200 && response.statusCode() < 300 ? response.body() : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private byte[] readBundledConfig() {
        try (InputStream input = new ClassPathResource("static/config.json").getInputStream()) {
            return input.readAllBytes();
        } catch (IOException ignored) {
            return null;
        }
    }

    private byte[] bundledDefaults() {
        return ("{\n  \"server_community_name\": \"" + DEFAULT_COMMUNITY_NAME
                + "\",\n  \"default_strings_api\": \"" + DEFAULT_STRINGS_API
                + "\",\n  \"fallback_unknown_card\": \"" + DEFAULT_FALLBACK_UNKNOWN_CARD
                + "\",\n  \"card_forbidden_url\": \"" + DEFAULT_CARD_FORBIDDEN_URL
                + "\",\n  \"card_limit_url\": \"" + DEFAULT_CARD_LIMIT_URL
                + "\",\n  \"card_semi_limit_url\": \"" + DEFAULT_CARD_SEMI_LIMIT_URL + "\"\n}\n")
                .getBytes(StandardCharsets.UTF_8);
    }

    private boolean apply(byte[] bytes) {
        try {
            ConfigFile config = objectMapper.readValue(bytes, ConfigFile.class);
            if (config == null
                    || isBlank(config.serverCommunityName)
                    || isBlank(config.defaultStringsApi)
                    || isBlank(config.fallbackUnknownCard)
                    || isBlank(config.cardLimitUrl)) {
                return false;
            }
            if (config.serverCommunityName != null && !config.serverCommunityName.isBlank()) {
                serverCommunityName = config.serverCommunityName;
            }
            if (config.defaultStringsApi != null && !config.defaultStringsApi.isBlank()) {
                defaultStringsApi = config.defaultStringsApi;
            }
            if (config.fallbackUnknownCard != null && !config.fallbackUnknownCard.isBlank()) {
                fallbackUnknownCard = config.fallbackUnknownCard;
            }
            if (config.cardForbiddenUrl != null && !config.cardForbiddenUrl.isBlank()) {
                cardForbiddenUrl = config.cardForbiddenUrl;
            }
            if (config.cardLimitUrl != null && !config.cardLimitUrl.isBlank()) {
                cardLimitUrl = config.cardLimitUrl;
            }
            if (config.cardSemiLimitUrl != null && !config.cardSemiLimitUrl.isBlank()) {
                cardSemiLimitUrl = config.cardSemiLimitUrl;
            }
            return true;
        } catch (IOException ignored) {
            // Keep safe defaults when a config source is malformed.
            return false;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void writeRootConfig(byte[] bytes) {
        try {
            Files.createDirectories(rootConfigPath.getParent());
            Path temporary = Files.createTempFile(rootConfigPath.getParent(), "config-", ".tmp");
            Files.write(temporary, bytes);
            try {
                Files.move(temporary, rootConfigPath, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, rootConfigPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception ignored) {
            // A read-only deployment can still use the bundled configuration.
        }
    }

    private static class ConfigFile {
        @JsonProperty("server_community_name")
        public String serverCommunityName;

        @JsonProperty("default_strings_api")
        public String defaultStringsApi;

        @JsonProperty("fallback_unknown_card")
        public String fallbackUnknownCard;

        @JsonProperty("card_forbidden_url")
        @JsonAlias("cardForbiddenUrl")
        public String cardForbiddenUrl;

        @JsonProperty("card_limit_url")
        @JsonAlias("cardLimitUrl")
        public String cardLimitUrl;

        @JsonProperty("card_semi_limit_url")
        @JsonAlias("cardSemiLimitUrl")
        public String cardSemiLimitUrl;
    }
}
