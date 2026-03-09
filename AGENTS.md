## Skills

### Available skills

- android-compose-mvvm: Build/refactor Kotlin + Compose + MVVM features in this repo while preserving layer boundaries and existing conventions. (file: .codex/skills/android-compose-mvvm/SKILL.md)
- android-verify-workflow: Run reliable local Android verification (build/tests/lint), including JDK setup and concise failure triage. (file: .codex/skills/android-verify-workflow/SKILL.md)
- android-gui-polish: Refine Compose UI layout for readability, compact one-screen fit, and tap efficiency in gameplay screens. (file: .codex/skills/android-gui-polish/SKILL.md)

### How to use skills

- Use `android-compose-mvvm` when adding or modifying app features, ViewModels, composables, domain models, or counting systems.
- Use `android-gui-polish` when improving screen density, visual hierarchy, spacing, and controls in Compose UI.
- Use `android-verify-workflow` after code changes and before handoff to confirm build health.
- Use these in sequence for UI tasks: `android-gui-polish` then `android-verify-workflow`.
- Use these in sequence for feature tasks: `android-compose-mvvm` then `android-verify-workflow`.
