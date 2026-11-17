package com.whenmo.repository.database;

import com.whenmo.model.database.domain.DeckPack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DeckPackRepository extends JpaRepository<DeckPack, Integer> {

    /**
     * 根據卡包名稱查詢
     */
    Optional<DeckPack> findByName(String name);

    /**
     * 根據使用者 ID 查詢其建立的所有卡包
     */
    List<DeckPack> findByUserId(Integer userId);

    /**
     * 查詢指定 packId 的單個公開卡包 (user_id 為 NULL)
     */
    @Query("SELECT dp FROM DeckPack dp WHERE dp.id = :packId AND dp.userId IS NULL")
    Optional<DeckPack> findPublicDeckPackById(@Param("packId") Integer packId);

    /**
     * 查詢指定 packId 且 user_id 等於指定值或為 NULL 的單個卡包
     */
    @Query("SELECT dp FROM DeckPack dp WHERE dp.id = :packId AND (dp.userId = :userId OR dp.userId IS NULL)")
    Optional<DeckPack> findByIdAndUserIdOrUserIdIsNull(@Param("packId") Integer packId,
            @Param("userId") Integer userId);

    /**
     * 查詢系統公用卡包（即 userId 為 null 的卡包）
     */
    @Query("SELECT dp FROM DeckPack dp WHERE dp.userId IS NULL")
    List<DeckPack> findPublicDeckPacks();

    /**
     * 查詢所有私人卡包（即 userId 不為 null 的卡包）
     */
    @Query("SELECT dp FROM DeckPack dp WHERE dp.userId IS NOT NULL")
    List<DeckPack> findPrivateDeckPacks();

    /**
     * 查詢系統公用卡包，是否有重複的 name
     */
    @Query("SELECT COUNT(dp) > 0 FROM DeckPack dp WHERE dp.name = :name AND dp.userId IS NULL")
    boolean existsByNameAndUserIsNull(@Param("name") String name);

    /**
     * 查詢系統公用卡包與指定使用者，是否有重複的 name
     */
    @Query("SELECT COUNT(dp) > 0 FROM DeckPack dp WHERE dp.name = :name AND (dp.userId IS NULL OR dp.userId = :userId)")
    boolean existsByNameAndUserIsNullOrEqualToUser(@Param("name") String name, @Param("userId") Integer userId);
}