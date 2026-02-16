# HZ Messenger

Android app template that builds a signed **Release APK** on every push using **GitHub Actions** and publishes it in **GitHub Releases**.

## Download APK
Open the **Releases** tab in your GitHub repo and download the latest APK.

## Notes
- The release build is signed with the default debug signing config for simplicity (works for APK installs outside Play).
- If you later want a persistent signing key (so updates install over previous versions), we can switch to a real keystore stored in GitHub Secrets.
