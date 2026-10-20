---
name: android-privacy-security
description: Harden this Android app for Play Store production with privacy, security, and policy checks: manifest exposure, permissions, backups, secrets, signing material, logging, network/security config, privacy policy alignment, Data safety answers, and safe local data handling. Use when reviewing app safety or implementing compliance-sensitive changes.
---

# Android Privacy Security

## Overview

Reduce avoidable Play policy risk and obvious Android security mistakes.
Prefer least privilege, minimal data collection, and explicit documentation of what the app does not collect.

## Workflow

1. Inspect app surface area:
- exported activities, services, receivers, and providers
- runtime and manifest permissions
- backup, restore, and data extraction settings
2. Inspect sensitive material handling:
- keystore files and signing config
- API keys, tokens, or secrets in source or resources
- debug logging that may expose behavior or user data
3. Align product claims with implementation:
- privacy policy
- Play Data safety form
- in-app disclosures when needed
4. Apply the smallest hardening changes that materially reduce risk.
5. Verify the app still builds with `android-verify-workflow`.

## Repo Notes

- This app currently appears to use local state only and no backend; preserve that simplicity unless the feature truly needs external services.
- Check `docs/privacy-policy.md` whenever app behavior changes.
- Check `app/src/main/AndroidManifest.xml`, `app/build.gradle.kts`, and any files under `keys/` or `keystore.properties` references before recommending release changes.

## Guardrails

- Do not add permissions, analytics SDKs, or network access casually; each one affects policy answers and user trust.
- Do not commit secrets, keystore passwords, or signing artifacts.
- Prefer removing unnecessary exposure over documenting it.
- If the app has no user accounts or personal data, keep the implementation and policy language consistent with that fact.
