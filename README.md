# Blackjack Counter (Android)

Blackjack Counter is a Jetpack Compose training app for live card-count practice.
It supports multiple counting systems, rule-aware strategy guidance, and a compact one-screen workflow.

## 30-Second Setup Checklist
- Open `Menu` -> `Table Rules` and set table conditions first.
- Open `Menu` -> `Select Counting System`.
- Set `Total Decks` and `Decks Remaining`.
- Start tapping card ranks as they are dealt.
- Use `Bet Recommendation` units with your chosen base bet.

## How To Use (Top-to-Bottom Flow)
1. Open `Menu` -> `Table Rules` and set the table first.
   Rule choices affect strategy actions and the estimated edge model.
2. Open `Menu` -> `Select Counting System` and choose your system.
   Some systems recommend Ace side count (`Hi-Opt II`, `Omega II`), which enables extra Ace tracking in deck controls.
3. In `Deck Controls`, set:
   - `Total Decks` (`1..8`)
   - `Decks Remaining` (slider, minimum `0.25`)
4. Tap card buttons as cards are dealt:
   - Row 1: `A`, `2`, `3`, `4`, `5`
   - Row 2: `6`, `7`, `8`, `9`, `T/J/Q/K`
5. Read the live metrics:
   - `Running` count
   - `True` count (`N/A` for running-count-only systems)
   - `Edge %` (only shown when true count is supported)
6. Use `Bet Recommendation` as a training ramp (units), then map units to your own base bet.
7. Use extras as needed:
   - `Help` for system explanations, tag values, and charts
   - `Menu` -> `Show/Hide Cheat Sheet`
   - `Strategy Chart` button for full-screen basic strategy + deviations
   - `Reset Count` to reset running count, shoe depth, and Ace side count

## Bet Units Explained
The app recommends a unit size from the ramp `1-2-4-8-12`.

- A "unit" is your base bet amount.
  Example: if `1 unit = $10`, then `8 units = $80`.
- For true-count systems (balanced systems), thresholds are:
  - `< 1.0` -> `1` unit
  - `1.0 to <2.0` -> `2` units
  - `2.0 to <3.0` -> `4` units
  - `3.0 to <4.0` -> `8` units
  - `>= 4.0` -> `12` units
- For running-count-only systems (unbalanced systems), thresholds are:
  - `< 2.0` -> `1` unit
  - `2.0 to <4.0` -> `2` units
  - `4.0 to <6.0` -> `4` units
  - `6.0 to <8.0` -> `8` units
  - `>= 8.0` -> `12` units

This is a training aid, not bankroll or gambling advice.

## Counting Systems
- Hi-Lo
- KO
- ReKO
- Hi-Opt II (Ace side count recommended)
- Omega II (Ace side count recommended)
- Zen Count
- Wong Halves

All systems implement a shared `CountingSystem` interface.

## Current UI (Quick Overview)
- Top bar:
  - `Help` button (always visible, beside `Menu`)
  - `Menu` actions:
    - Select Counting System
    - Table Rules
    - Reset Count
    - Show/Hide Cheat Sheet
- Main stats:
  - Running Count
  - True Count (or `N/A`)
  - Estimated Edge %
  - Bet Recommendation (units)
- Deck controls:
  - Total Decks (`1..8`)
  - Decks Remaining slider (`0.25` minimum)
  - Penetration %
  - Cards Drawn / Cards Left / Total Cards
  - Ace side count panel when selected system recommends it
- Card input (2 rows):
  - `A`, `2`, `3`, `4`, `5`
  - `6`, `7`, `8`, `9`, `T/J/Q/K`
- Bottom actions:
  - Reset Count
  - Strategy Chart (full-screen table with back button)

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
- Chart updates from current count context (when supported)
- Rule-aware action adjustments
- Deviation cells marked with `*`
- Split deviation guidance available in Help

## Estimated Edge %
Estimated Edge % is computed from:
- current true count (if supported by the selected system)
- selected table rules

It is a simplified training estimate.

## Tech Stack
- Kotlin
- Jetpack Compose
- Material 3
- MVVM (`ViewModel` + `StateFlow`)
- Local state only (no backend, no database)

## Requirements
- Android Studio (latest stable recommended)
- JDK 11+
- `compileSdk` 36
- `minSdk` 24

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

Optional verification script:
- `powershell -ExecutionPolicy Bypass -File .codex/skills/android-verify-workflow/scripts/verify.ps1`

## Project Structure
- `app/src/main/java/com/yourname/blackjackcounter/data/counting`
  - Counting system interface + implementations
- `app/src/main/java/com/yourname/blackjackcounter/domain/model`
  - App state, card/rule models, and core calculations
- `app/src/main/java/com/yourname/blackjackcounter/presentation/counter`
  - `CounterViewModel`, screen, and reusable UI components
- `app/src/main/java/com/yourname/blackjackcounter/ui/theme`
  - Theme, typography, and colors

## Notes
- Deck safety floor is enforced to avoid divide-by-zero behavior.
- True count is rounded to one decimal place for display.
- Educational/training use only.
