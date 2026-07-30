# PetLingo Kids 3.0

幼兒英文互動闖關版。

## 3.0 新功能

- 玳瑁貓、虎斑貓、長毛吉娃娃三位吉祥物
- 吉祥物簡易上下動態效果
- 答對時正向鼓勵動畫與提示音
- 10 關聽音選圖關卡
- 星星與成就系統
- 幼兒大圖卡：英文、中文、TextToSpeech 發音
- 保留 `toeic_words.tsv` 進階字庫（目前檔案含 8000 筆，可搜尋 7000+ 單字）
- 套件 ID 改為 `com.petlingo.kids`，可與舊版並存
- GitHub Actions 使用 JDK 17，成功後自動上傳 APK

## GitHub 建置

1. 將本 ZIP 解壓後所有檔案上傳到 GitHub Repository 根目錄。
2. 刪除 Repository 內其他舊 `.github/workflows/*.yml`。
3. 到 **Actions → Build PetLingo Kids 3.0 APK → Run workflow**。
4. 完成後，在 Artifacts 下載 `PetLingo-Kids-3.0-APK`。
5. 解壓 Artifact，安裝 `PetLingo-Kids-3.0.apk`。

## 安裝提醒

新版套件名稱是 `com.petlingo.kids`，正常情況可與 `com.petlingo.app` 舊版同時存在。
