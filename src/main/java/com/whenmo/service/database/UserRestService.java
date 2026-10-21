package com.whenmo.service.database;

import com.whenmo.model.database.domain.*;
import com.whenmo.model.database.request.*;
import com.whenmo.model.database.response.*;
import com.whenmo.repository.database.UserRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Service
@Validated
@RequiredArgsConstructor
public class UserRestService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ============================================================
    // Read
    // ============================================================

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(UserResponse::fromModel).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Integer id) {
        return UserResponse.fromModel(getById(id));
    }

    @Transactional(readOnly = true)
    public UserResponse login(UserLoginRequest request) {
        String name = request.getName() == null ? null : request.getName().trim();
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("用戶名稱或密碼錯誤");
        }

        User user = userRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("用戶名稱或密碼錯誤"));

        if (request.getPassword() == null || request.getPassword().isEmpty() || user.getPasswordHash() == null
                || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("用戶名稱或密碼錯誤");
        }

        return UserResponse.fromModel(user);
    }

    // ============================================================
    // Create
    // ============================================================

    @Transactional
    public UserResponse createUser(@Valid @RequestBody UserCreateRequest request) {
        String name = request.getName() == null ? null : request.getName().trim();
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("用戶名稱不可為空");
        }
        if (userRepository.existsByName(name)) {
            throw new IllegalArgumentException("用戶名稱已存在: " + name);
        }

        // 檢查資料庫是否完全沒有用戶，若沒有則自動賦予管理員權限
        if (userRepository.count() == 0) {
            request.setIsAdmin(true);
        }

        User user = request.toModel(passwordEncoder);
        user.setName(name);

        return UserResponse.fromModel(userRepository.save(user));
    }

    // ============================================================
    // Update
    // ============================================================

    @Transactional
    public UserResponse updateUser(Integer id, UserUpdateRequest request) {
        User user = getById(id);

        String newName = request.getName();
        if (newName != null && !user.getName().equals(newName)) {
            if (userRepository.existsByName(newName)) {
                throw new IllegalArgumentException("用戶名稱已存在: " + newName);
            }
        }

        request.updateModel(user, passwordEncoder);
        return UserResponse.fromModel(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateProfile(Integer id, UserProfileUpdateRequest request) {
        User user = getById(id);
        boolean changingName = request.getName() != null;
        boolean changingPassword = request.getOldPassword() != null || request.getNewPassword() != null;

        if (changingName == changingPassword) {
            throw new IllegalArgumentException("請一次只修改名稱或密碼");
        }

        if (changingName) {
            String newName = request.getName().trim();
            if (newName.isEmpty()) {
                throw new IllegalArgumentException("用戶名稱不可為空");
            }
            if (!user.getName().equals(newName) && userRepository.existsByName(newName)) {
                throw new IllegalArgumentException("用戶名稱已存在: " + newName);
            }
            user.setName(newName);
        } else {
            String oldPassword = request.getOldPassword();
            String newPassword = request.getNewPassword();
            if (oldPassword == null || oldPassword.isEmpty() || newPassword == null || newPassword.isEmpty()) {
                throw new IllegalArgumentException("舊密碼與新密碼不可為空");
            }
            if (user.getPasswordHash() == null || !passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
                throw new IllegalArgumentException("舊密碼錯誤");
            }
            user.setPasswordHash(passwordEncoder.encode(newPassword));
        }

        return UserResponse.fromModel(userRepository.save(user));
    }

    // ============================================================
    // Delete
    // ============================================================

    @Transactional
    public void deleteUser(Integer id) {
        userRepository.delete(getById(id));
    }

    // ============================================================
    // Helpers
    // ============================================================

    public User getById(Integer id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("找不到對應的用戶ID: " + id));
    }
}