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

# Verify (preferred before release — handles JDK detection)
powershell -ExecutionPolicy Bypass -File .codex/skills/android-verify-workflow/scripts/verify.ps1 -IncludeUnitTests -IncludeLint
```

Use `gradlew.bat` instead of `./gradlew` on Windows CMD.

## Architecture

Single-module Android app (`com.vectorlogic.blackjackcounter`) using Kotlin + Jetpack Compose + MVVM. It is a one-screen blackjack card-counting trainer.

**Package layers:**
- `data/counting/` — Pluggable counting system implementations behind the `CountingSystem` interface
- `data/monetization/` — AdMob, UMP consent, and Play Billing integration
- `domain/model/` — Pure data/logic classes (`CounterState`, `CounterCalculations`, `TableRules`, etc.)
- `presentation/counter/` — `CounterViewModel` (main state machine) + `CounterScreen` + `components/`
- `ui/theme/` — Material 3 design tokens

**State flow:** `CounterViewModel` owns a single `StateFlow<CounterState>`. Composables call ViewModel methods on user interaction and react to state via `collectAsState()`. `CounterState` is an immutable data class (~30 fields).

**Counting systems:** `CountingSystem` interface is implemented by 7 systems (Hi-Lo, KO, ReKO, Hi-Opt II, Omega II, Zen Count, Wong Halves). New systems must implement the interface and register in `CounterViewModel`. Balanced systems use true count; unbalanced (KO, ReKO) do not.

**Monetization tiers:**
- Free: Hi-Lo, KO, ReKO + banner ads
- Pro ($1.99): ad removal
- Full ($3.99): all 7 systems + ad removal

`MonetizationRepository` / `PlayBillingMonetizationRepository` handle billing. `AdsConsentManager` wraps UMP consent before showing AdMob ads.

## Key Files

| File | Purpose |
|------|---------|
| `MainActivity.kt` | Entry point; initializes UMP + AdMob, sets `CounterScreen` |
| `CounterViewModel.kt` | 1200-line state machine; all business logic lives here |
| `CounterState.kt` | Immutable state snapshot |
| `CounterCalculations.kt` | True count, edge %, bet recommendation math |
| `TableRules.kt` | 10 configurable rule enums (S17/H17, peek, payout, etc.) |
| `gradle/libs.versions.toml` | Centralized dependency versions |

## Guardrails

- **Deck safety:** `decksRemaining` minimum is 0.25 (prevents divide-by-zero in true count). Total decks capped 1–8.
- **Entitlement trust:** Entitlement tier must be re-validated from billing, not cached client-side unsafely.
- **Release checklist** is in `README.md` — follow it before submitting to Play Store.
- **Debug variant** uses `.debug` package suffix and test AdMob IDs (from `src/debug/`).

## Skills

`.codex/skills/` contains reproducible agent workflows:
- `android-compose-mvvm` — Feature development following MVVM + Compose conventions
- `android-verify-workflow` — Build/test/lint verification with JDK auto-detection
- `android-gui-polish` — UI refinement for compact one-screen layout
- `playstore-monetization` — AdMob/UMP/Billing configuration
