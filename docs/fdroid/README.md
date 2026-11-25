# F-Droid

**Status (2026-11-26):** merged. [fdroiddata!53737](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/53737) (fork `vectorlogic-dev/fdroiddata`, branch `com.vectorlogic.blackjackcounter`) was merged on 2026-11-25, after a tester's report found no problems: the app works as described, makes no network connections, has no trackers or runtime permissions, and VirusTotal flagged nothing. The app shows up at https://f-droid.org/packages/com.vectorlogic.blackjackcounter/ after F-Droid's next build and publish run, which can take several days. Once that page is live, add an F-Droid link to the main README and the website.

- `com.vectorlogic.blackjackcounter.yml` is the fdroiddata recipe for v1.1.0 (versionCode 10, commit `1f945d9`), identical to the file in the merge request.
- Store text, icon, changelog and screenshots are in `fastlane/metadata/android/en-US/`.
- The published v1.1.0 APK rebuilds byte for byte from the tag, so the recipe uses `Binaries` and `AllowedAPKSigningKeys`: F-Droid ships the APK signed with our key, and users can move between GitHub and F-Droid builds without reinstalling.

## Submitting

This is how v1.1.0 got in, kept for reference. Later releases don't need a new merge request unless the build recipe changes.

1. Fork https://gitlab.com/fdroid/fdroiddata and add `com.vectorlogic.blackjackcounter.yml` as `metadata/com.vectorlogic.blackjackcounter.yml`.
2. Open a merge request with the "App inclusion" template. Mention that the build is reproducible and the signing key is pinned.
3. Watch the merge request pipeline and answer reviewer comments. Expected questions: the `prebuild` line and the categories. Don't add a `sudo` block: F-Droid's build server already has JDK 21, and reviewers ask for `sudo` to be removed when it isn't needed.

What the first pipeline caught, now fixed in the recipe:

- **GitLab identity verification.** Pipelines on a new fork don't start until the GitLab account verifies its identity. Without that, leave a note asking a maintainer to trigger CI.
- **`rewritemeta` formatting.** CI's `rewritemeta` wraps the long `Binaries:` URL onto its own indented line, with a trailing space after `Binaries:`. The PyPI release of fdroidserver formats it differently, so check against CI's output, not a local run.
- **Scanner and the foojay resolver.** The scanner rejects the `org.gradle.toolchains.foojay-resolver-convention` plugin in `settings.gradle.kts`, so `prebuild` deletes that line. The scanner also removes `gradle/gradle-daemon-jvm.properties`. A v1.1.0 rebuild with both changes is still byte for byte identical to the published APK. Both are gone from the repo after v1.1.0, so the `prebuild` line has nothing left to do.

## Every future release

F-Droid picks up new versions from git tags (`UpdateCheckMode: Tags`), so nothing needs editing in fdroiddata as long as each release keeps:

- the tag `v<versionName>` and one asset named `CountingEdge-<versionName>.apk`, as `scripts/publish-release.sh` produces;
- a higher `versionCode` and a matching `fastlane/metadata/android/en-US/changelogs/<versionCode>.txt` (500 characters max);
- a reproducible build: release from a clean, committed tree with JDK 21, and leave `dependenciesInfo` disabled in `app/build.gradle.kts`.

New build entries copy the previous one's fields, including the `prebuild` that strips the foojay plugin. The plugin is no longer in `settings.gradle.kts`, so on later versions that `sed` matches nothing and does no harm. It can be dropped from new build entries whenever the recipe is next edited.

To check reproducibility before or after a release, build `assembleRelease` from a fresh clone of the tag (no `keystore.properties`, so the APK is unsigned) and compare it with the published APK minus its signing block. The two must be identical.

Refresh the screenshots in `fastlane/.../phoneScreenshots/` when the UI changes noticeably.
