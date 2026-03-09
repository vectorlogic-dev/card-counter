# Blackjack Counter (Android)

Blackjack Counter is a Jetpack Compose training app for live card-count practice.

It supports multiple counting systems, rule-aware strategy guidance, and a compact one-screen counter workflow.

## Current UI (Quick Overview)
- Top bar:
  - `Help` button (always visible, beside `Menu`)
  - `Menu` actions:
    - Select Counting System
    - Table Rules
    - Reset Count
    - Reset Shoe
    - Show/Hide Cheat Sheet
- Main stats:
  - Running Count
  - True Count (or `N/A` for systems that do not use true count)
  - Estimated Edge %
  - Bet Recommendation (units)
- Deck controls:
  - Total Decks (`1..8`)
  - Decks Remaining slider (`0.25` minimum)
  - Penetration %
  - Cards Drawn / Cards Left / Total Cards
  - Ace side count panel when selected system recommends it
- Card input:
  - `A`, `2`, `3`, `4`
  - `5`, `6`, `7`, `8`
  - `9`, `T/J/Q/K`
- Bottom actions:
  - Reset Count
  - Open Strategy Chart (full-screen table with back button)

## Counting Systems
- Hi-Lo
- KO
- ReKO
- Hi-Opt II (ace side count recommended)
- Omega II (ace side count recommended)
- Zen Count
- Wong Halves

All systems implement a common `CountingSystem` interface for modular expansion.

## Bet Recommendation
- The app displays a bet ramp recommendation in units.
- Default ramp: `1-2-4-8-12`
- Uses:
  - True Count for balanced systems
  - Running Count for unbalanced systems

This is a training aid, not bankroll advice.

## Table Rules Supported
- Dealer soft 17: `S17` / `H17`
- Dealer peek:
  - `ACE_AND_TEN`
  - `ACE_ONLY` (no peek on 10)
  - `NO_PEEK`
- Blackjack payout: `3:2` / `6:5`
- Double down:
  - Any two cards
  - 9 to 11 only
  - 10 to 11 only
- Double after split (DAS)
- Surrender: none / late / early
- Resplit aces
- Hit split aces
- Max split hands: `2..6`
- 6-card Charlie auto win

## Strategy and Deviations
- Full-screen strategy chart in table form (hard/soft/pairs)
- Chart updates from current count context (when supported by system)
- Rule-aware action adjustments
- Deviation cells marked with `*`
- Split deviation guidance available in Help

## Estimated Edge %
Estimated Edge % is computed from:
- current true count (if supported)
- selected table rules

It is a simplified estimate for practice/training.

## Tech Stack
- Kotlin
- Jetpack Compose
- Material 3
- MVVM (`ViewModel` + `StateFlow`)
- Local state only (no backend, no database)

## Requirements
- Android Studio (latest stable recommended)
- JDK 11+
- compileSdk 36
- minSdk 24

## Run (Android Studio)
1. Open this folder in Android Studio.
2. Wait for Gradle sync.
3. Select an emulator or physical device.
4. Run the `app` configuration.

## Build / Test (CLI)
Windows:
- `.\gradlew.bat assembleDebug`
- `.\gradlew.bat testDebugUnitTest`

macOS/Linux:
- `./gradlew assembleDebug`
- `./gradlew testDebugUnitTest`

Optional repo verification script:
- `powershell -ExecutionPolicy Bypass -File .codex/skills/android-verify-workflow/scripts/verify.ps1`

## Project Structure
- `app/src/main/java/com/yourname/blackjackcounter/data/counting`
  - Counting system interface + implementations
- `app/src/main/java/com/yourname/blackjackcounter/domain/model`
  - App state, card/rule models, and core calculations
- `app/src/main/java/com/yourname/blackjackcounter/presentation/counter`
  - `CounterViewModel`, screen, and reusable UI components
- `app/src/main/java/com/yourname/blackjackcounter/ui/theme`
  - Theme, typography, and color system

## Notes
- Deck safety floor is enforced to avoid divide-by-zero behavior.
- True count is rounded to one decimal place for display.
- This app is intended for educational/training use.
