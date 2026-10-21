package com.whenmo.service.database;

import com.whenmo.model.database.domain.*;
import com.whenmo.model.database.request.*;
import com.whenmo.model.database.response.*;
import com.whenmo.repository.database.DiyerRepository;
import com.whenmo.repository.database.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import java.util.List;
import java.util.Set;

@Service
@Validated
@RequiredArgsConstructor
public class DiyerRestService {

    private final DiyerRepository diyerRepository;
    private final UserRepository userRepository;

    // ============================================================
    // Read
    // ============================================================

    @Transactional(readOnly = true)
    public List<DiyerResponse> getAllDiyers() {
        return diyerRepository.findAll().stream().map(DiyerResponse::fromModel).toList();
    }

    @Transactional(readOnly = true)
    public DiyerResponse getDiyerById(Integer id) {
        return DiyerResponse.fromModel(getById(id));
    }

    @Transactional(readOnly = true)
    public List<DiyerResponse> searchDiyersByName(String name, boolean eq) {
        if (name == null || name.isBlank()) {
            if (eq) {
                throw new RuntimeException("找不到作者: " + name);
            } else {
                return getAllDiyers();
            }
        }

        if (eq) {
            return List.of(DiyerResponse.fromModel(diyerRepository.findByName(name)
                    .orElseThrow(() -> new RuntimeException("找不到對應的作者名稱: " + name))));
        }

        return diyerRepository.findByNameContainingIgnoreCase(name.trim()).stream().map(DiyerResponse::fromModel)
                .toList();
    }

    // ============================================================
    // Create
    // ============================================================

    @Transactional
    public DiyerResponse createDiyer(DiyerCreateRequest request) {
        if (diyerRepository.existsByName(request.getName()))
            throw new IllegalArgumentException("作者名稱已存在: " + request.getName());

        validateCodeRanges(request.getCodeRanges());
        validateSetRanges(request.getSetRanges());

        Diyer diyer = request.toDiyerWithoutUser();

        return DiyerResponse.fromModel(diyerRepository.save(diyer));
    }

    // ============================================================
    // Update
    // ============================================================

    @Transactional
    public DiyerResponse updateDiyer(DiyerUpdateRequest request) {
        Diyer diyer = getById(request.getId());

        String newName = request.getName();
        if (newName != null && !diyer.getName().equals(newName) && diyerRepository.existsByName(newName)) {
            throw new IllegalArgumentException("作者名稱已存在: " + newName);
        }

        if (request.getCodeRanges() != null)
            validateCodeRanges(request.getCodeRanges());
        if (request.getSetRanges() != null)
            validateSetRanges(request.getSetRanges());

        if (request.getUserId() != null) {
            User currentBoundUser = diyer.getUser();
            if (currentBoundUser == null || !currentBoundUser.getId().equals(request.getUserId()))
                bindUserToDiyer(diyer, request.getUserId());
        }

        request.updateDiyerWithoutUser(diyer);
        return DiyerResponse.fromModel(diyerRepository.save(diyer));
    }

    // ============================================================
    // Delete
    // ============================================================

    @Transactional
    public void deleteDiyer(Integer id) {
        diyerRepository.delete(getById(id));
    }

    // ============================================================
    // Internal Validations & Helpers
    // ============================================================

    private Diyer getById(Integer id) {
        return diyerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到對應的 Diyer，ID: " + id));
    }

    private void bindUserToDiyer(Diyer diyer, Integer userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("找不到對應的用戶ID: " + userId));

        Diyer boundDiyer = diyerRepository.findByUserId(userId).orElse(null);
        if (boundDiyer != null && !boundDiyer.getId().equals(diyer.getId())) {
            throw new IllegalArgumentException("用戶ID: " + userId + " 已綁定至作者ID: " + boundDiyer.getId());
        }
        diyer.setUser(user);
    }

    private void validateCodeRanges(Set<Integer> numberRanges) {
        if (numberRanges == null)
            return;
        for (Integer range : numberRanges) {
            if (range == null || range < 1 || range > 2000) {
                throw new IllegalArgumentException("卡號區間必須介於 1 ~ 2000 之間: " + range);
            }
        }
    }

    private void validateSetRanges(Set<Integer> setRanges) {
        if (setRanges == null)
            return;
        for (Integer range : setRanges) {
            if (range == null || range < 1 || range > 255) {
                throw new IllegalArgumentException("字段區間必須介於 0x01~0xFF 之間: " + range);
            }
        }
    }
}