package com.whenmo.api.database;

import com.whenmo.config.SecurityFilterProperties;
import com.whenmo.model.database.request.UserDeckUploadRequest;
import com.whenmo.model.database.request.DeckPackCreateRequest;
import com.whenmo.model.database.request.UserDeckEditRequest;
import com.whenmo.model.database.request.UserDeckVisibilityRequest;
import com.whenmo.model.database.response.UserDeckResponse;
import com.whenmo.model.database.response.UserDeckViewResponse;
import com.whenmo.service.database.UserDeckRestService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public ResponseEntity<List<UserDeckResponse>> getPublicDecks() {
        return ResponseEntity.ok(deckService.getPublicDecks());
    }

    @GetMapping("/mine")
    public ResponseEntity<List<UserDeckResponse>> getMyDecks(HttpServletRequest request) {
        Integer userId = getAuthenticatedUserId(request);
        if (userId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(deckService.getUserDecks(userId));
    }

    @GetMapping("/packs")
    public ResponseEntity<List<String>> getDeckPacks(HttpServletRequest request) {
        if (getAuthenticatedUserId(request) == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(deckService.getDeckPacks());
    }

    @PostMapping("/packs")
    public ResponseEntity<String> createDeckPack(
            @RequestBody DeckPackCreateRequest requestBody,
            HttpServletRequest request) {
        if (getAuthenticatedUserId(request) == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(deckService.createDeckPack(requestBody));
    }

    @GetMapping("user/{userId}/{deckId}")
    public ResponseEntity<UserDeckViewResponse> getDeckForView(
            @PathVariable Integer userId,
            @PathVariable Integer deckId,
            HttpServletRequest request) {
        Integer viewerId = getAuthenticatedUserId(request);
        return deckService.getDeckForView(userId, deckId, viewerId).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/mine/{deckId}")
    public ResponseEntity<UserDeckViewResponse> getDeckForEdit(
            @PathVariable Integer deckId,
            HttpServletRequest request) {
        Integer userId = getAuthenticatedUserId(request);
        if (userId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return deckService.getDeckForEdit(userId, deckId).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ============================================================
    // Create
    // ============================================================

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDeckResponse> uploadDeck(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String packName,
            @RequestParam(defaultValue = "false") Boolean isPublic,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        Integer userId = getAuthenticatedUserId(request);
        if (userId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        UserDeckUploadRequest uploadRequest = new UserDeckUploadRequest(name, isPublic, packName);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(deckService.uploadDeck(userId, uploadRequest, file));
    }

    // ============================================================
    // Update
    // ============================================================

    @PutMapping("/{deckId}/visibility")
    public ResponseEntity<UserDeckResponse> updateVisibility(
            @PathVariable Integer deckId,
            @RequestBody UserDeckVisibilityRequest requestBody,
            HttpServletRequest request) {
        Integer userId = getAuthenticatedUserId(request);
        if (userId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(deckService.updateVisibility(userId, deckId, requestBody));
    }

    @PutMapping("/{deckId}")
    public ResponseEntity<UserDeckResponse> editDeck(
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