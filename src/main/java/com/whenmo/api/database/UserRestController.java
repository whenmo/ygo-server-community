package com.whenmo.api.database;

import com.whenmo.model.database.request.*;
import com.whenmo.model.database.response.*;
import com.whenmo.config.SecurityFilterProperties;
import com.whenmo.service.database.UserRestService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserRestController {

    private final UserRestService userService;
    private final SecurityFilterProperties securityProps;

    // ============================================================
    // Read
    // ============================================================

    /**
     * 查詢所有使用者
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * 根據 ID 查詢使用者
     * GET /api/users/id/{id}
     */
    @GetMapping("/id/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // ============================================================
    // Create / Auth
    // ============================================================

    /**
     * 建立/註冊新使用者
     * POST /api/users
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserResponse createdUser = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    /**
     * 使用者登入驗證
     * POST /api/users/login
     */
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody UserLoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    // ============================================================
    // Update
    // ============================================================

    /**
     * 更新使用者資訊
     * PUT /api/users/id/{id}
     */
    @PutMapping("/id/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Integer id,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    /**
     * 更新目前登入使用者的個人資料或密碼
     * PUT /api/users/profile
     */
    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @RequestBody UserProfileUpdateRequest request,
            HttpServletRequest httpRequest) {
        Integer userId = getAuthenticatedUserId(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }

    private Integer getAuthenticatedUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;

        Object userId = session.getAttribute("userId");
        Object sessionCredential = session.getAttribute(securityProps.getSessionKey());
        if (!(userId instanceof Integer) || sessionCredential == null) return null;

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

    // ============================================================
    // Delete
    // ============================================================

    /**
     * 刪除使用者
     * DELETE /api/users/id/{id}
     */
    @DeleteMapping("/id/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}