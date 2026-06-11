================================================================================
LOANZZONES SUPPORT APP — Google Play UPLOAD KEY (generated locally)
================================================================================
Package: com.loanzzone.support

FILES IN THIS FOLDER:
  upload-keystore.jks      -> KEEP SECRET. Back up to a safe place (USB/cloud vault).
  upload_certificate.pem   -> Upload THIS file in Play Console (Upload key reset).
  READ_ME_FIRST_UPLOAD_KEY.txt (this file) -> Contains passwords. DELETE after saving elsewhere.

KEYSTORE SETTINGS:
  Alias:           upload
  Keystore file:   upload-keystore.jks
  Keystore pass:   ht8psFVcvtyxuRNDxy5FMUAAq3GK
  Key pass:        ht8psFVcvtyxuRNDxy5FMUAAq3GK   (same as keystore)

NEXT STEPS:
  1. In Play Console: Request upload key reset -> upload upload_certificate.pem
  2. After Google approves: sign release builds with upload-keystore.jks (alias: upload)
  3. Add signing to app/build.gradle.kts or use Android Studio Generate Signed Bundle

  NEVER commit the keystore folder to git (it is in .gitignore).

Generated: 2026-04-02 16:51:12
================================================================================
