package com.whenmo.service.database;

import com.whenmo.model.database.domain.*;
import com.whenmo.model.database.request.*;
import com.whenmo.model.database.response.*;
import com.whenmo.repository.database.DiyerRepository;
import com.whenmo.service.RestServiceHelper;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiyerRestService {

    private final DiyerRepository diyerRepository;
    private final EntityManager entityManager;

    // ============================================================
    // Read
    // ============================================================

    @Transactional(readOnly = true)
    public List<DiyerResponse> getAllDiyers() {
        return diyerRepository.findAll().stream().map(DiyerResponse::fromDiyer).toList();
    }

    @Transactional(readOnly = true)
    public DiyerResponse getDiyerById(Integer diyerId) {
        return DiyerResponse.fromDiyer(getById(diyerId));
    }

    @Transactional(readOnly = true)
    public List<DiyerResponse> getDiyersByCdbId(Integer cdbId) {
        return diyerRepository.findByCdbId(cdbId).stream().map(DiyerResponse::fromDiyer).toList();
    }

    @Transactional(readOnly = true)
    public List<DiyerResponse> getDiyersByUserId(Integer userId) {
        return diyerRepository.findByUserId(userId).stream().map(DiyerResponse::fromDiyer).toList();
    }

    // ============================================================
    // Create
    // ============================================================

    @Transactional
    public DiyerResponse createDiyer(DiyerCreateRequest request) {
        String name = getValidDiyerName(request.getName(), request.getCdbId());

        Diyer diyer = request.toDiyer(name);

        List<DiyerString> fullStrings = transToDiyerString(request.getStrings(), diyer);

        diyer.setStrings(fullStrings);

        return DiyerResponse.fromDiyer(refreshDiyer(diyerRepository.save(diyer)));
    }

    // ============================================================
    // Update
    // ============================================================

    @Transactional
    public DiyerResponse updateDiyer(Integer diyerId, DiyerUpdateRequest request) {
        Diyer diyer = getById(diyerId);

        String name = diyer.getName();
        if (request.getName() != null && !request.getName().equals(name)) {
            name = getValidDiyerName(request.getName(), diyer.getCdbId());
        }

        List<DiyerString> fullStrings = transToDiyerString(request.getStrings(), diyer);

        request.updateDiyer(diyer, name, fullStrings);

        return DiyerResponse.fromDiyer(refreshDiyer(diyerRepository.save(diyer)));
    }

    // ============================================================
    // Delete
    // ============================================================

    @Transactional
    public void deleteDiyer(Integer id) {
        diyerRepository.delete(getById(id));
    }

    // ============================================================
    // Helper
    // ============================================================

    private Diyer getById(Integer diyerId) {
        return diyerRepository.findById(diyerId).orElseThrow(() -> new RuntimeException("找不到對應的 Diyer，ID: " + diyerId));
    }

    private String getValidDiyerName(String name, Integer cdbId) {
        String validName = RestServiceHelper.getValidTrimString(name, "作者名稱不可為空");
        if (diyerRepository.existsByCdbIdAndName(cdbId, validName))
            throw new IllegalArgumentException("作者名稱已在 cdb 存在: " + validName);
        return validName;
    }

    private List<DiyerString> transToDiyerString(List<DiyerStringEditRequest> editStrings, Diyer diyer) {
        if (editStrings == null || editStrings.isEmpty())
            return new ArrayList<>();

        return editStrings.stream().map(request -> {
            String trimName = RestServiceHelper.getValidTrimString(request.getName(), "diyerString 名稱不可為空");
            return request.toDiyerString(trimName, diyer);
        }).toList();
    }

    private Diyer refreshDiyer(Diyer diyer) {
        entityManager.flush();
        entityManager.refresh(diyer);
        return diyer;
    }
}