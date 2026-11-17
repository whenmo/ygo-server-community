package com.whenmo.controller.api.database;

import com.whenmo.model.database.request.*;
import com.whenmo.model.database.response.*;
import com.whenmo.controller.helper.SecurityHelper;
import com.whenmo.service.database.UserRestService;

import jakarta.servlet.http.HttpServletRequest;
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
    private final SecurityHelper securityHelper;

    // ============================================================
    // GET
    // ============================================================

    /**
     * 查詢所有用戶
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * 根據 ID 查詢用戶
     * GET /api/users/id/{id}
     */
    @GetMapping("/id/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // ============================================================
    // POST
    // ============================================================

    /**
     * 建立/註冊新用戶
     * POST /api/users
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserResponse createdUser = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    /**
     * 用戶登入驗證
     * POST /api/users/login
     */
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody UserLoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    // ============================================================
    // PUT
    // ============================================================

    /**
     * 管理員更新用戶資訊
     * PUT /api/users/admin/info/{id}
     */
    @PutMapping("/admin/info/{id}")
    public ResponseEntity<UserResponse> updateUserInfo(HttpServletRequest httpRequest,
            @PathVariable Integer id,
            @Valid @RequestBody UserUpdateInfoRequest request) {

        if (securityHelper.getAuthUserId(httpRequest) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else if (!securityHelper.isAuthAdmin(httpRequest)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(userService.userUpdateInfo(id, request));
    }

    /**
     * 管理員更新用戶密碼
     * PUT /api/users/admin/password/{id}
     */
    @PutMapping("/admin/password/{id}")
    public ResponseEntity<UserResponse> updateUserPassword(HttpServletRequest httpRequest,
            @PathVariable Integer id,
            @Valid @RequestBody UserUpdatePasswordRequest request) {

        if (securityHelper.getAuthUserId(httpRequest) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else if (!securityHelper.isAuthAdmin(httpRequest)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(userService.userUpdatePassword(id, request));
    }

    /**
     * 用戶更新用戶資訊
     * PUT /api/users/profile/info
     */
    @PutMapping("/profile/info")
    public ResponseEntity<UserResponse> updateUserInfo(HttpServletRequest httpRequest,
            @Valid @RequestBody UserUpdateInfoRequest request) {

        Integer userId = securityHelper.getAuthUserId(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(userService.userUpdateInfo(userId, request));
    }

    /**
     * 用戶更新用戶密碼
     * PUT /api/users/profile/password
     */
    @PutMapping("/profile/password")
    public ResponseEntity<UserResponse> updateUserPassword(HttpServletRequest httpRequest,
            @Valid @RequestBody UserUpdatePasswordRequest request) {

        Integer userId = securityHelper.getAuthUserId(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(userService.userUpdatePassword(userId, request));
    }

    // ============================================================
    // DELETE
    // ============================================================

    /**
     * 管理員/用戶刪除用戶
     * DELETE /api/users/id/{id}
     */
    @DeleteMapping("/id/{id}")
    public ResponseEntity<Void> deleteUser(HttpServletRequest httpRequest, @PathVariable Integer id) {

        Integer userId = securityHelper.getAuthUserId(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else if (!userId.equals(id) && !securityHelper.isAuthAdmin(httpRequest)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}