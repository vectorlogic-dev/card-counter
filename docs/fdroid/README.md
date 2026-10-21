# F-Droid

**Status (2026-10-22):** ready to submit, merge request not opened yet.

- `com.vectorlogic.blackjackcounter.yml` is the fdroiddata recipe for v1.1.0 (versionCode 10, commit `1f945d9`). It passes `fdroid lint` and `fdroid rewritemeta`.
- Store text, icon, changelog and screenshots are in `fastlane/metadata/android/en-US/`.
- The published v1.1.0 APK rebuilds byte for byte from the tag, so the recipe uses `Binaries` and `AllowedAPKSigningKeys`: F-Droid ships the APK signed with our key, and users can move between GitHub and F-Droid builds without reinstalling.

## Submitting

1. Fork https://gitlab.com/fdroid/fdroiddata and add `com.vectorlogic.blackjackcounter.yml` as `metadata/com.vectorlogic.blackjackcounter.yml`.
2. Open a merge request with the "App inclusion" template. Mention that the build is reproducible and the signing key is pinned.
3. Watch the merge request pipeline and answer reviewer comments. Expected questions: the `sudo` JDK 21 install (the Gradle daemon needs JDK 21) and the categories.

## Every future release

F-Droid picks up new versions from git tags (`UpdateCheckMode: Tags`), so nothing needs editing in fdroiddata as long as each release keeps:

- the tag `v<versionName>` and one asset named `CountingEdge-<versionName>.apk`, as `scripts/publish-release.sh` produces;
- a higher `versionCode` and a matching `fastlane/metadata/android/en-US/changelogs/<versionCode>.txt` (500 characters max);
- a reproducible build: release from a clean, committed tree with JDK 21, and leave `dependenciesInfo` disabled in `app/build.gradle.kts`.

To check reproducibility before or after a release, build `assembleRelease` from a fresh clone of the tag (no `keystore.properties`, so the APK is unsigned) and compare it with the published APK minus its signing block. The two must be identical.

Refresh the screenshots in `fastlane/.../phoneScreenshots/` when the UI changes noticeably.
