package com.whenmo.api.database;

import com.whenmo.config.SecurityFilterProperties;
import com.whenmo.model.database.request.DeckPackCreateRequest;
import com.whenmo.model.database.request.UserDeckEditRequest;
import com.whenmo.model.database.response.DeckResponse;
import com.whenmo.model.database.response.UserResponse;
import com.whenmo.service.database.UserDeckRestService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.*;

import java.util.List;

@RestController
@RequestMapping("/api/decks")
@RequiredArgsConstructor
public class UserDeckRestController {

    private final UserDeckRestService deckService;
    private final SecurityFilterProperties securityProps;

    // ============================================================
    // Read
    // ============================================================

    @GetMapping("/public")
    public ResponseEntity<List<DeckResponse>> getPublicDecks() {
        return ResponseEntity.ok(deckService.getPublicDecks());
    }

    @GetMapping("/mine")
    public ResponseEntity<List<DeckResponse>> getMyDecks(HttpServletRequest request) {
        Integer userId = getAuthenticatedUserId(request);
        if (userId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(deckService.getUserDecks(userId));
    }

    @GetMapping("/packNames")
    public ResponseEntity<List<String>> getDeckPacks() {
        return ResponseEntity.ok(deckService.getDeckPacks());
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getDeckUsers() {
        return ResponseEntity.ok(deckService.getDeckUsers());
    }

    @GetMapping("user/{userId}/{deckId}")
    public ResponseEntity<DeckResponse> getDeckForView(
            @PathVariable Integer userId,
            @PathVariable Integer deckId,
            HttpServletRequest request) {
        Integer viewerId = getAuthenticatedUserId(request);
        return deckService.getDeckForView(userId, deckId, viewerId).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/mine/{deckId}")
    public ResponseEntity<DeckResponse> getDeckForEdit(@PathVariable Integer deckId, HttpServletRequest request) {
        Integer userId = getAuthenticatedUserId(request);
        if (userId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return deckService.getDeckForEdit(userId, deckId).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{ownerId}/{deckId}/download")
    public ResponseEntity<Resource> downloadDeckFile(
            @PathVariable Integer ownerId,
            @PathVariable Integer deckId,
            HttpServletRequest request) {
        Integer viewerId = getAuthenticatedUserId(request);
        try {
            Resource resource = deckService.loadDeckFileResource(ownerId, deckId, viewerId);
            String filename = resource.getFilename();

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename*=UTF-8''"
                                    + java.net.URLEncoder.encode(filename, java.nio.charset.StandardCharsets.UTF_8))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
    // ============================================================
    // Create
    // ============================================================

    @PostMapping("/packs")
    public ResponseEntity<String> createDeckPack(
            @RequestBody DeckPackCreateRequest requestBody,
            HttpServletRequest request) {
        if (getAuthenticatedUserId(request) == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(deckService.createDeckPack(requestBody));
    }

    @PostMapping
    public ResponseEntity<DeckResponse> createDeck(
            @RequestBody UserDeckEditRequest requestBody,
            HttpServletRequest request) {
        Integer userId = getAuthenticatedUserId(request);
        if (userId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(deckService.createDeck(userId, requestBody));
    }

    // ============================================================
    // Update
    // ============================================================

    @PutMapping("/{deckId}")
    public ResponseEntity<DeckResponse> editDeck(
            @PathVariable Integer deckId,
            @RequestBody UserDeckEditRequest requestBody,
            HttpServletRequest request) {
        Integer userId = getAuthenticatedUserId(request);
        if (userId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(deckService.editDeck(userId, deckId, requestBody));
    }

    // ============================================================
    // Delete
    // ============================================================

    @DeleteMapping("/{deckId}")
    public ResponseEntity<Void> deleteDeck(@PathVariable Integer deckId, HttpServletRequest request) {
        Integer userId = getAuthenticatedUserId(request);
        if (userId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        deckService.deleteDeck(userId, deckId);
        return ResponseEntity.noContent().build();
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