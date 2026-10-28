# ygo-server-community

`ygo-server-community` 是一個基於 **Spring Boot** 與 **MVC 架構** 開發的遊戲王（Yu-Gi-Oh!）服務器社群與卡片檢索網站，旨在為玩家提供便捷的卡片查詢、交流以及牌組分享平台。

---

## 技術棧 (Tech Stack)

* **後端核心**：Spring Boot, Spring Data JPA, Hibernate
* **資料庫**：SQLite（支援解析與查詢 `.cdb` 卡片資料庫及 `strings.conf` 配置檔及 `iflist.conf` 配置檔）
* **前端與視圖**：Thymeleaf, HTML5, CSS3, JavaScript (結合前端互動元件)
* **版控與工具**：Maven, Git

---

## 核心功能 (Key Features)

* **卡片檢索系統**：
  * 支援快速查詢遊戲王卡片資料。
  * 可透過卡片名稱、系列、關鍵字或相關屬性進行精確與模糊搜尋。
* **分享卡組功能**：
  * 讓玩家能夠在平台建立、編輯並儲存自己的遊戲王牌組。
  * 支援將牌組內容公開分享給社群其他成員檢視與交流。
* **社群互動架構**：
  * 採經典且高效的 MVC 設計模式，便於維護與擴充模組。

---

## 當前開發目標 (Current Goal)

目前專案正在積極開發與完善的核心功能為：

---

### 環境需求
* JDK 25
* Maven
