# PetLingo 全新 Android 專案

這不是從舊版 UI 繼續修改，而是重新建立的乾淨 Android Compose 專案。

## 主要修正

- 新 package：`com.petlingo.app`
- 首頁只使用 `petlingo_hero.png`
- Hero 圖直接從確認過的三寵物畫面裁切
- 完整顯示虎斑貓、長毛吉娃娃、玳瑁貓
- 不使用舊版 `PetHero` 疊字或裁切程式
- 固定使用 `ContentScale.Fit`
- 固定使用原圖寬高比 `976 / 474`
- App 內沒有任何真實寵物照片
- 桌面圖示、Adaptive Icon 重新建立
- 保留 8,000 筆學習資料

## 建置

1. 解壓縮並把所有檔案上傳到 GitHub Repository 根目錄。
2. 開啟 Actions。
3. 執行 `Build PetLingo New Project APK`。
4. 下載 `PetLingo-NewProject` Artifact。

APK：`PetLingo-NewProject.apk`

## 注意

因 applicationId 改為 `com.petlingo.app`，這會被 Android 視為全新的 App，不會覆蓋舊版。這正是為了避免舊版快取、資源名稱與安裝資料繼續影響畫面。


## Gradle 啟動修正

本專案根目錄現在包含：

- `gradlew`
- `gradlew.bat`

GitHub Actions 會先以 `gradle/actions/setup-gradle` 安裝 Gradle 8.10.2，
再由 `./gradlew` 將建置參數轉交給已安裝的 Gradle，因此下列指令可正常執行：

```bash
chmod +x gradlew
./gradlew :app:assembleDebug
```

這個啟動腳本是為 GitHub Actions 建置環境設計，不包含二進位
`gradle-wrapper.jar`。如果要在完全沒有安裝 Gradle 的電腦離線建置，
需要另外由 Android Studio 或 Gradle 產生官方 Wrapper。
