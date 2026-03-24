## Skills

### Available skills

- android-compose-mvvm: Build/refactor Kotlin + Compose + MVVM features in this repo while preserving layer boundaries and existing conventions. (file: .codex/skills/android-compose-mvvm/SKILL.md)
- android-verify-workflow: Run reliable local Android verification (build/tests/lint), including JDK setup and concise failure triage. (file: .codex/skills/android-verify-workflow/SKILL.md)
- android-gui-polish: Refine Compose UI layout for readability, compact one-screen fit, and tap efficiency in gameplay screens. (file: .codex/skills/android-gui-polish/SKILL.md)
- android-architecture: Guide Android app structure, MVVM boundaries, repositories/use cases, dependency decisions, and maintainable refactors. (file: .codex/skills/android-architecture/SKILL.md)
- android-debugger: Diagnose Android crashes, build failures, manifest issues, state bugs, lifecycle mistakes, and Gradle/configuration problems. (file: .codex/skills/android-debugger/SKILL.md)
- android-release-readiness: Prepare the app for Google Play release work, including versioning, signing, bundle generation, release hardening, rollout checks, and Play Console readiness. (file: .codex/skills/android-release-readiness/SKILL.md)
- android-privacy-security: Review privacy, security, exported components, permissions, secrets, backups, logging, and Play policy/Data safety alignment. (file: .codex/skills/android-privacy-security/SKILL.md)
- android-observability-quality: Add production quality gates such as tests, crash/ANR monitoring, analytics discipline, and release-blocking validation. (file: .codex/skills/android-observability-quality/SKILL.md)
- playstore-monetization: Evaluate monetization model, conversion surfaces, Play listing strategy, and billing/ad tradeoffs. (file: .codex/skills/playstore-monetization/SKILL.md)

### How to use skills

- Use `android-compose-mvvm` when adding or modifying app features, ViewModels, composables, domain models, or counting systems.
- Use `android-gui-polish` when improving screen density, visual hierarchy, spacing, and controls in Compose UI.
- Use `android-architecture` when refactoring packages, introducing new layers, or making maintainability decisions that affect more than one screen.
- Use `android-debugger` when diagnosing crashes, bad state, Gradle errors, or suspicious runtime behavior.
- Use `android-release-readiness` when preparing uploads, changing signing/versioning, or auditing whether the app is ready for a Play track.
- Use `android-privacy-security` when app behavior changes could affect permissions, secrets, telemetry, exported components, privacy policy, or Data safety answers.
- Use `android-observability-quality` when adding tests, crash reporting, analytics, performance checks, or release gates.
- Use `playstore-monetization` when planning ads, billing, premium features, onboarding for conversion, or listing strategy.
- Use `android-verify-workflow` after code changes and before handoff to confirm build health.
- Use these in sequence for UI tasks: `android-gui-polish` then `android-verify-workflow`.
- Use these in sequence for feature tasks: `android-compose-mvvm` then `android-verify-workflow`.
- Use these in sequence for architecture work: `android-architecture` then `android-compose-mvvm` then `android-verify-workflow`.
- Use these in sequence for debugging: `android-debugger` then `android-verify-workflow`.
- Use these in sequence for production hardening: `android-privacy-security` then `android-observability-quality` then `android-release-readiness` then `android-verify-workflow`.
