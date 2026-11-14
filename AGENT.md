# AGENT.md — CivitDeck (fork `bpawnzZ/CivitDeck`)

Kotlin Multiplatform client for Civitai (Android / iOS / desktop). Upstream is
`rioX432/CivitDeck`; this fork adds an iOS release pipeline and feature work.

## Build & test (Linux, no macOS)

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk      # jdk17-openjdk; jre17 fails with "No Java compiler found"
./gradlew --no-daemon :shared:jvmTest              # JVM unit tests (all shared suites)
./gradlew --no-daemon :feature:feature-gallery:jvmTest
./gradlew --no-daemon detekt                       # static analysis, ~2 min
```

- **Android SDK is not installed** on this box — never run Android/assemble tasks; they fail on SDK
  location, not on your change.
- iOS/Kotlin-Native code (`shared/src/iosMain`, all Swift) **cannot be compiled locally**. The only
  real compile check for iOS is the `release.yml` `build-ios` job (macOS runner).
- `~/go/bin/actionlint` validates workflow YAML; run it after touching `.github/workflows`.

## CI / release

- `.github/workflows/release.yml` — `workflow_dispatch` input `ios_only` (bool) builds just the iOS
  IPA; a tag push (`v*`) builds everything and publishes a GitHub release.
  `build-android` is gated on `github.repository_owner == 'rioX432'` (fork has no signing secrets).
  iOS job runs on `macos-26`, builds with `CODE_SIGNING_ALLOWED=NO`, stubs the version from the tag
  with `plutil`, and uploads `CivitDeck-<version>-ios-unsigned.ipa` (+ `.sha256`).
- Unsigned on purpose: SideStore/AltStore re-sign on device. Bundle id and team id are upstream's.
- Dispatch one run and watch it with `gh run watch <id> --repo bpawnzZ/CivitDeck --exit-status`
  (never a sleep loop).

## Traps

- **New Swift files must be registered by hand** in `iosApp/iosApp.xcodeproj/project.pbxproj`
  (PBXBuildFile + PBXFileReference + group child + Sources phase) — the project has no
  file-system-synchronized groups. A missed entry is a compile error, not a silent skip.
- **Never nest `NavigationStack`**: a pushed destination that owns its own stack resets the parent
  path (the Collections screen bounced back for exactly this reason). Use `NavigationLink(value:)`
  with destinations registered on the stack that already exists.
- Version strings live in `iosApp/iosApp/Info.plist` (`2.5`/`10` hardcoded) — CI overwrites them;
  do not "fix" them for a build.
- `NSAllowsArbitraryLoads` is enabled upstream for LAN ComfyUI/A1111 servers — leave it.
- `civitai.red` and `civitai.com` share one API; the app pins `https://civitai.com/api/v1`.
- Public API reads need no key: `/api/v1/images` (with `type=image|video`), `/api/v1/articles`,
  `/api/v1/articles/{id}` (HTML body). `/api/v1/bounties` and challenges are 404 (tRPC only).

## Change workflow

`openspec` is initialised (`openspec/`). Any code change: `openspec new change <slug>` → proposal /
specs / design / tasks → `openspec validate <slug> --strict` → implement → archive. Current and past
changes live in `openspec/changes/`.

## Current state

- Fork master == release `v2.5.2` line: NSFW blur settings now apply to browse cards, collection
  navigation fixed (no nested stack), CI builds/publishes unsigned IPAs.
- `openspec/changes/add-browse-content-feeds` — Images / Videos / Articles browse surfaces (KMP
  data layer + iOS UI). Shared layer is per-platform-neutral so Android/desktop only need UI.
