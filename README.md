# Watcher — Android app

A real Kotlin + Jetpack Compose Android app that is a **control panel client** for the
existing Watcher backend (`artifacts/api-server` in the Watcher-main repo — Express +
Postgres, already deployed). It does not embed any scraping logic itself, per the spec:
the backend owns monitoring; this app lists/creates/pauses/deletes monitors, shows
per-source status and history, and edits settings.

## Why there's no compiled .apk file attached

I built this in a sandboxed environment whose network egress is limited to a short
allowlist (npm/pip/crates/GitHub package registries). It has **no access to**:
- Google's Maven repo (`dl.google.com`) — needed for every AndroidX/Compose/Material3 dependency
- Gradle's distribution server (`services.gradle.org`)
- The Android SDK manager

I checked directly — both returned `403` from the network proxy. So I cannot actually
invoke a Gradle build or produce a signed/debug `.apk` binary from here. Rather than
fabricate a fake "build succeeded" result (which would violate the same "no fake
adapters" principle the original spec insists on), I'm giving you the complete,
real source project plus the easiest paths to an actual compiled APK.

## Getting the actual APK — pick one

**Option A — GitHub Actions (no local setup, ~3 minutes)**
1. Push this `watcher-android/` folder into the Watcher-main repo (or its own repo) on GitHub.
2. The included `.github/workflows/android-build.yml` runs on every push to `main`, builds
   `assembleDebug`, and uploads `watcher-debug-apk` as a workflow artifact.
3. Go to the Actions tab → the run → Artifacts → download `watcher-debug-apk`, unzip, install
   `app-debug.apk` on your phone (enable "install unknown apps" for whichever app you used to
   open it).

**Option B — Android Studio, locally**
1. Open this folder in Android Studio (Koala/2024.1+ recommended).
2. Let it sync Gradle (this downloads AndroidX/Compose from Google's Maven — works fine on a
   normal internet connection).
3. Build → Build Bundle(s) / APK(s) → Build APK(s), or just hit Run with a device/emulator
   connected.

**Option C — command line, locally**
```
cd watcher-android
gradle wrapper --gradle-version 8.7   # generates gradlew if you don't already have Gradle
./gradlew assembleDebug
# APK lands at app/build/outputs/apk/debug/app-debug.apk
```

## First launch

1. Open the app → Settings tab → paste your backend's base URL (e.g.
   `https://your-replit-app.repl.co/api/`) → "Save & reconnect".
2. Go to Monitors → "+" → pick Product or Movie → fill the form → start monitoring.
3. Grant the notification permission when prompted (Android 13+).

## Notifications — read this before assuming it's real-time push

There is **no server push** wired up (the backend has no `/device/register` endpoint or
Firebase Admin credentials, and I don't have a Firebase project to fabricate one with).
Instead, `WatcherSyncWorker` (WorkManager) polls the backend roughly every 15–30 minutes
— 15 minutes is Android's hard minimum for periodic background work, and Doze/battery
optimization can push it further out. It compares each target's status against what it
last saw on this device and fires a local high-priority notification on a genuine
transition (e.g. `unavailable → available`, `booking_closed → booking_open`) — it never
re-notifies for `available → available`.

This is deliberately not claimed to be the reliable 10-minute push the original spec
described, because Android genuinely can't guarantee that from an app process alone —
the spec itself says "if the Android platform prevents truly reliable background
execution, do not fake it." Wiring real push (so notifications work instantly, even with
the app force-closed) needs three things none of which I can fabricate: a Firebase
project you create, a `/device/register` endpoint added to the backend, and the backend
calling Firebase Admin itself when *it* detects a transition (it already runs every 10
minutes server-side).

## What's implemented vs. still open

Implemented for real: full CRUD against `/monitors`, `/monitors/{id}/check`,
`/monitors/{id}/history`, `/dashboard`, `/sources`, `/settings`; a working nav (Monitors /
History / Settings, matching the spec's simple bottom nav); Product and Movie add-forms;
monitor detail with per-source/per-date status; local notification pipeline with real
(not simulated) transition detection.

Still open, honestly: no Firebase/push, no deep-link handling into the actual retailer/
booking apps from a tapped notification (the tap currently reopens the app to the
monitor, since the backend's `MonitorTarget.url` field would need to be forwarded — easy
follow-up once you want it), no offline caching/Room database, no instrumented UI tests.
