# Installing LifeDots

Use Android 8.0 or later. Select **Settings → Theme → Liquid Glass** for the new wallpaper and app appearance. The theme is saved with your existing preferences; selecting another theme restores that appearance without changing effect settings.

## Local APK for a phone

Run `./gradlew assembleSideload` with JDK 17 and the Android SDK configured in `local.properties`.
Install `app/build/outputs/apk/sideload/app-sideload.apk`.
It appears as **LifeDots Preview**, uses `com.example.lifedots.sideload`, and is signed with this computer's debug key. It can coexist with the original LifeDots, with separate settings. It is for local testing, not store distribution. Keep the same debug keystore for future updates to this preview.

For an update to an existing debug installation, build `./gradlew assembleDebug` and use `app/build/outputs/apk/debug/app-debug.apk`. The installed app must have the same signing certificate.

## Release signing

Plain release builds remain unsigned unless a signing key is supplied. Unsigned APKs cannot be installed. Use Android Studio's **Generate Signed App Bundle / APK**, or set `LIFEDOTS_KEYSTORE` (absolute path), `LIFEDOTS_STORE_PASSWORD`, `LIFEDOTS_KEY_ALIAS`, and `LIFEDOTS_KEY_PASSWORD` in your local environment and run `./gradlew assembleRelease`. Use the original signing key to update an existing installation. Never commit keys or passwords.

## If Android says “App not installed”

That message alone does not identify the cause. With USB debugging enabled and your phone connected, run:

```
adb devices
adb install -r app/build/outputs/apk/sideload/app-sideload.apk
```

Record the `INSTALL_FAILED_...` result. A certificate mismatch (`UPDATE_INCOMPATIBLE`) needs the original signing key or the separate Preview build. A version downgrade needs a build with an appropriate version code. A missing certificate needs a signed APK. Do not uninstall your existing app just to diagnose this: uninstalling removes its local settings.

When opening an APK on the phone, allow installation for the specific file manager/browser you use if Android requests it. Transfer the complete APK, not an AAB or an unsigned release artifact.

Reference: https://developer.android.com/studio/publish/app-signing
