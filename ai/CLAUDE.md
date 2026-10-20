# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Build
./gradlew assembleDebug           # Debug APK
./gradlew assembleRelease         # Release APK (minified + shrunk)
./gradlew bundleRelease           # Release AAB for Play Store

# Test
./gradlew testDebugUnitTest       # Run all unit tests
./gradlew testDebugUnitTest --tests "*.CounterCalculationsTest"  # Single test class

# Lint
./gradlew lintDebug

# Verify (preferred before release, handles JDK detection)
powershell -ExecutionPolicy Bypass -File .codex/skills/android-verify-workflow/scripts/verify.ps1 -IncludeUnitTests -IncludeLint
```

Use `gradlew.bat` instead of `./gradlew` on Windows CMD.

## Architecture

Single-module Android app (`com.vectorlogic.blackjackcounter`) using Kotlin + Jetpack Compose + MVVM. It is a one-screen blackjack card-counting trainer.

**Package layers:**
- `data/counting/` - Pluggable counting system implementations behind the `CountingSystem` interface
- `domain/model/` - Pure data/logic classes (`CounterState`, `CounterCalculations`, `TableRules`, etc.)
- `presentation/counter/` - `CounterViewModel` (main state machine) + `CounterScreen` + `components/`
- `ui/theme/` - Material 3 design tokens

**State flow:** `CounterViewModel` owns a single `StateFlow<CounterState>`. Composables call ViewModel methods on user interaction and react to state via `collectAsState()`.

**Counting systems:** `CountingSystem` interface is implemented by 7 systems (Hi-Lo, KO, ReKO, Hi-Opt II, Omega II, Zen Count, Wong Halves). Balanced systems use true count; unbalanced (KO, ReKO) do not.

## Key Files

| File | Purpose |
|------|---------|
| `MainActivity.kt` | Entry point; sets `CounterScreen` |
| `CounterViewModel.kt` | Main state machine; business logic lives here |
| `CounterState.kt` | Immutable state snapshot |
| `CounterCalculations.kt` | True count, edge %, bet recommendation math |
| `TableRules.kt` | Configurable rule enums (S17/H17, peek, payout, etc.) |
| `gradle/libs.versions.toml` | Centralized dependency versions |

## Guardrails

- **Deck safety:** `decksRemaining` minimum is `0.25` (prevents divide-by-zero in true count). Total decks capped at `1..8`.
- **Release checklist** is in `README.md`.
- **Debug variant** uses `.debug` package suffix.

## Skills

`.codex/skills/` contains reproducible agent workflows:
- `android-compose-mvvm` - Feature development following MVVM + Compose conventions
- `android-verify-workflow` - Build/test/lint verification with JDK auto-detection
- `android-gui-polish` - UI refinement for compact one-screen layout
