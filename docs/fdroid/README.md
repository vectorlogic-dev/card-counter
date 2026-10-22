# F-Droid

**Status (2026-10-23):** submitted as [fdroiddata!53737](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/53737) from the fork `vectorlogic-dev/fdroiddata`, branch `com.vectorlogic.blackjackcounter`. The full fdroiddata pipeline passes, including the build and the reproducible-APK check. Waiting for F-Droid review.

- `com.vectorlogic.blackjackcounter.yml` is the fdroiddata recipe for v1.1.0 (versionCode 10, commit `1f945d9`), identical to the file in the merge request.
- Store text, icon, changelog and screenshots are in `fastlane/metadata/android/en-US/`.
- The published v1.1.0 APK rebuilds byte for byte from the tag, so the recipe uses `Binaries` and `AllowedAPKSigningKeys`: F-Droid ships the APK signed with our key, and users can move between GitHub and F-Droid builds without reinstalling.

## Submitting

1. Fork https://gitlab.com/fdroid/fdroiddata and add `com.vectorlogic.blackjackcounter.yml` as `metadata/com.vectorlogic.blackjackcounter.yml`.
2. Open a merge request with the "App inclusion" template. Mention that the build is reproducible and the signing key is pinned.
3. Watch the merge request pipeline and answer reviewer comments. Expected questions: the `sudo` JDK 21 install (the Gradle daemon needs JDK 21), the `prebuild` line and the categories.

What the first pipeline caught, now fixed in the recipe:

- **GitLab identity verification.** Pipelines on a new fork don't start until the GitLab account verifies its identity. Without that, leave a note asking a maintainer to trigger CI.
- **`rewritemeta` formatting.** CI's `rewritemeta` wraps the long `Binaries:` URL onto its own indented line, with a trailing space after `Binaries:`. The PyPI release of fdroidserver formats it differently, so check against CI's output, not a local run.
- **Scanner and the foojay resolver.** The scanner rejects the `org.gradle.toolchains.foojay-resolver-convention` plugin in `settings.gradle.kts`, so `prebuild` deletes that line. The scanner also removes `gradle/gradle-daemon-jvm.properties`. A v1.1.0 rebuild with both changes is still byte for byte identical to the published APK.

## Every future release

F-Droid picks up new versions from git tags (`UpdateCheckMode: Tags`), so nothing needs editing in fdroiddata as long as each release keeps:

- the tag `v<versionName>` and one asset named `CountingEdge-<versionName>.apk`, as `scripts/publish-release.sh` produces;
- a higher `versionCode` and a matching `fastlane/metadata/android/en-US/changelogs/<versionCode>.txt` (500 characters max);
- a reproducible build: release from a clean, committed tree with JDK 21, and leave `dependenciesInfo` disabled in `app/build.gradle.kts`.

New build entries copy the previous one's fields, including the `prebuild` that strips the foojay plugin. Removing the plugin from `settings.gradle.kts` would make that line a harmless no-op.

To check reproducibility before or after a release, build `assembleRelease` from a fresh clone of the tag (no `keystore.properties`, so the APK is unsigned) and compare it with the published APK minus its signing block. The two must be identical.

Refresh the screenshots in `fastlane/.../phoneScreenshots/` when the UI changes noticeably.
