---
name: android-release-readiness
description: Prepare this Android app for Google Play production releases, including versioning, signing, release build hardening, app bundle generation, Play Console rollout steps, pre-launch report triage, and release checklists. Use when shipping a release, auditing production readiness, or setting up a repeatable release workflow.
---

# Android Release Readiness

## Overview

Ship a Play Store release with a repeatable, low-surprise workflow.
Treat release readiness as build health plus store readiness plus rollback awareness.

## Workflow

1. Confirm release identity first:
- package name
- versionCode and versionName
- target track: internal, closed, open, or production
2. Verify release build configuration:
- signing config resolves correctly
- `release` is non-debuggable
- shrinking and minification stay enabled unless there is a proven blocker
- app bundle output is preferred for Play uploads
3. Run release-grade checks:
- `assembleRelease`
- `bundleRelease`
- `lintRelease`
- unit tests when code changed materially
4. Review Play-facing readiness:
- app name, icon, screenshots, feature graphic, privacy policy URL
- release notes and tester-facing change summary
- Data safety answers still match real app behavior
5. Triage launch blockers before upload:
- crashers from pre-launch report
- missing 64-bit/native symbol handling if native code exists
- signing/version conflicts
- policy-sensitive copy or screenshots
6. End with a concise go/no-go checklist.

## Repo Notes

- Read `app/build.gradle.kts` for versioning, build types, shrinking, and signing setup.
- Read `app/src/main/AndroidManifest.xml` for exported components and app identity.
- Read `README.md` and `docs/privacy-policy.md` before suggesting Play Console submission steps.

## Guardrails

- Do not disable minification or shrinkers as a convenience fix for release issues without naming the tradeoff.
- Prefer `.aab` generation over APK distribution for Play.
- Call out missing release assets or policy items explicitly instead of assuming they exist.
- Treat Play Console answers as product commitments; keep them aligned with the real app.
