# PetLingo Rebuilt

全新重建的 Android 英文學習 App，採 Kotlin、Jetpack Compose、Material 3 與 Java 17。

## 已完成

- 英式／美式 TTS 發音切換
- 單字搜尋與收藏
- 測驗即時正確／錯誤提示
- 錯題本與詳細學習紀錄
- 每次測驗保存題目、作答、正解、分數、說明與時間
- Android Speech Recognizer 口說辨識
- 0–100 文字相似度口說練習評分
- 關閉 App 後仍保存紀錄、收藏與口音設定
- GitHub Actions 自動產生 Debug APK

## 建置

GitHub：推送到 `main`，開啟 Actions，下載 `PetLingo-APK` artifact。

Android Studio：使用 JDK 17 開啟專案，等待 Gradle Sync 後執行 `app`。

> 口說分數是手機語音辨識結果與目標文字的相似度，適合日常練習，不是專業音素評測。
