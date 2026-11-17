package com.whenmo.service.database;

import com.whenmo.model.database.domain.*;
import com.whenmo.model.database.request.*;
import com.whenmo.model.database.response.*;
import com.whenmo.repository.database.UserRepository;
import com.whenmo.service.RestServiceHelper;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserRestService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ============================================================
    // Read
    // ============================================================

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(UserResponse::fromUser).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Integer id) {
        return UserResponse.fromUser(getById(id));
    }

    @Transactional(readOnly = true)
    public UserResponse login(UserLoginRequest request) {
        String name = RestServiceHelper.getValidTrimString(request.getName(), "用戶名稱或密碼錯誤");
        String password = RestServiceHelper.getValidTrimString(request.getPassword(), "用戶名稱或密碼錯誤");

        User user = userRepository.findByName(name).orElseThrow(() -> new IllegalArgumentException("用戶名稱或密碼錯誤"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("用戶名稱或密碼錯誤");
        }

        return UserResponse.fromUser(user);
    }

    // ============================================================
    // Create
    // ============================================================

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        String password = RestServiceHelper.getValidTrimString(request.getPassword(), "密碼不可為空");

        String name = getValidUserrName(request.getName());

        User user = request.toUser(name, passwordEncoder.encode(password));

        if (userRepository.count() == 0) {
            user.setIsAdmin(true);
        }

        return UserResponse.fromUser(userRepository.save(user));
    }

    // ============================================================
    // Update
    // ============================================================

    @Transactional
    public UserResponse updateUser(Integer userId, UserUpdateRequest request) {
        User user = getById(userId);

        String name = null;
        if (request.getName() != null && request.getName().equals(user.getName()))
            name = getValidUserrName(request.getName());

        String password = null;
        if (request.getNewPassword() != null)
            password = RestServiceHelper.getValidTrimString(request.getNewPassword(), "新密碼不可為空");

        request.updateUser(user, name, password);
        return UserResponse.fromUser(userRepository.save(user));
    }

    @Transactional
    public UserResponse userUpdateInfo(Integer userId, UserUpdateInfoRequest request) {
        User user = getById(userId);

        String name = getValidUserrName(request.getName());
        if (user.getName().equals(name))
            throw new IllegalArgumentException("新名稱與舊名稱不能相同");

        request.updateModel(user, name);
        return UserResponse.fromUser(userRepository.save(user));
    }

    @Transactional
    public UserResponse userUpdatePassword(Integer userId, UserUpdatePasswordRequest request) {
        User user = getById(userId);

        String oldPassword = RestServiceHelper.getValidTrimString(request.getOldPassword(), "舊密碼不可為空");
        String newPassword = RestServiceHelper.getValidTrimString(request.getNewPassword(), "新密碼不可為空");

        if (oldPassword.equals(newPassword)) {
            throw new IllegalArgumentException("新密碼與舊密碼不能相同");
        } else if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("舊密碼錯誤");
        }

        request.updateUser(user, passwordEncoder.encode(newPassword));

        return UserResponse.fromUser(userRepository.save(user));
    }

    // ============================================================
    // Delete
    // ============================================================

    @Transactional
    public void deleteUser(Integer userId) {
        userRepository.delete(getById(userId));
    }

    // ============================================================
    // Helpers
    // ============================================================

    private User getById(Integer userId) {
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("找不到對應的用戶ID: " + userId));
    }

    public String getNameById(Integer userId) {
        return getById(userId).getName();
    }

    private String getValidUserrName(String name) {
        String validName = RestServiceHelper.getValidTrimString(name, "用戶名稱不可為空");
        if (userRepository.existsByName(validName))
            throw new IllegalArgumentException("用戶名稱已存在: " + validName);
        return validName;
    }

}