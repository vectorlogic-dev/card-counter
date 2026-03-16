# Counting Edge: Card Counter (Android)

Counting Edge: Card Counter is a Jetpack Compose training app for live card-count practice.
It supports multiple counting systems, rule-aware strategy guidance, and a compact one-screen workflow.

## Current Release Target
- Channel: `Closed testing` (Google Play)
- Version name: `1.0.0-closed-testing.1`
- Version code: `8`

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
3. In `Deck Controls`, set:
   - `Total Decks` (segmented button: `1 / 2 / 4 / 6 / 8`)
   - `Decks Remaining` (slider, minimum `0.25`)
4. Tap card buttons as cards are dealt:
   - Row 1: `A`, `2`, `3`, `4`, `5`
   - Row 2: `6`, `7`, `8`, `9`, `10-K`
5. Read the live metrics:
   - `Running` count
   - `True` count (`N/A` for running-count-only systems)
   - `Edge %` (only shown when true count is supported)
6. Use `Bet Recommendation` as a training ramp (units), then map units to your own base bet.
7. Use extras as needed:
   - `Help` for system explanations, tag values, and charts
   - `Menu` -> `Show/Hide Cheat Sheet`
   - `Strategy Chart` button for full-screen basic strategy + deviations
   - `Recent Cards` panel + `Undo` for correcting missed taps
   - `Reset Count` to reset shoe depth and count state
   - For `KO` / `ReKO`, reset returns to the deck-specific initial running count (IRC)

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
For balanced systems, decision thresholds use **truncated true count** by default.

For `KO` / `ReKO`, ramps are deck-aware and keyed from the system's `Key Count` and `Pivot`:
- `< Key Count` -> `1` unit
- `>= Key Count` -> `2` units
- `>= Pivot` -> `4` units
- `>= Pivot + 4` -> `8` units
- `>= Pivot + 8` -> `12` units

Unbalanced-system reset references are deck-specific:
- `KO` IRC formula: `4 - (4 * decks)` (special case for `1` deck = `0`)
- `ReKO` IRC formula: `1 - (3 * decks)`
- Example on `6` decks: `KO = -20`, `ReKO = -17`

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

All counting systems are available in the app at no extra cost.

## Current UI (Quick Overview)
- Header (floating, no card):
  - Current system name in gold
  - Info icon -> Help dialog
  - Menu icon -> Select Counting System, Table Rules, Reset Count, Show/Hide Cheat Sheet
- Main stats row (3 cards with colored top accent strip):
  - Running Count
  - True Count (or `N/A` for unbalanced systems)
  - Estimated Edge %
- Bet Recommendation card:
  - Large unit number on the right; five pip dots on the left indicating position on the `1 -> 2 -> 4 -> 8 -> 12` ramp
- Deck controls:
  - Segmented button: `1 / 2 / 4 / 6 / 8` decks
  - Decks Remaining slider (`0.25` minimum)
  - Penetration %
  - Cards Drawn / Cards Left / Total Cards
  - Ace side count panel when selected system recommends it
- Card input (2 rows, each button shows rank + tag value):
  - `A`, `2`, `3`, `4`, `5`
  - `6`, `7`, `8`, `9`, `10-K`
- Bottom actions:
  - Recent Cards panel with badge chips + Undo (item count adapts for compact layouts)
  - Reset Count and Strategy Chart (full-screen table)

## Table Rules Supported
- Dealer soft 17: `S17` / `H17`
- Dealer peek: `ACE_AND_TEN`, `ACE_ONLY`, `NO_PEEK`
- Blackjack payout: `3:2` / `6:5`
- Double down: any two cards, 9 to 11 only, or 10 to 11 only
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
- total decks in shoe

Rule and deck adjustments are calibrated from Wizard of Odds rule-variation deltas (8-deck baseline model, then adjusted by selected deck count).

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
- Package/application ID: `com.vectorlogic.blackjackcounter`

## Run (Android Studio)
1. Open this folder in Android Studio.
2. Wait for Gradle sync.
3. Select an emulator or physical device.
4. Run the `app` configuration.

## Build / Test (CLI)
Windows:
- `./gradlew.bat assembleDebug`
- `./gradlew.bat testDebugUnitTest`

macOS/Linux:
- `./gradlew assembleDebug`
- `./gradlew testDebugUnitTest`

Optional verification script:
- `powershell -ExecutionPolicy Bypass -File .codex/skills/android-verify-workflow/scripts/verify.ps1`

## Release Checklist
- Build and lint release before upload:
  - `./gradlew.bat assembleRelease lintRelease`
- Build the upload bundle (`.aab`) for Play Console:
  - `./gradlew.bat bundleRelease`

## Privacy Policy (Play Console)
- Privacy policy file in this repo: `docs/privacy-policy.md`
- Host this file on a public HTTPS URL (for example via GitHub Pages) and paste that URL into Play Console -> App content -> Privacy Policy.

## Project Structure
- `app/src/main/java/com/vectorlogic/blackjackcounter/data/counting`
  - Counting system interface + implementations
- `app/src/main/java/com/vectorlogic/blackjackcounter/domain/model`
  - App state, card/rule models, and core calculations
- `app/src/main/java/com/vectorlogic/blackjackcounter/presentation/counter`
  - `CounterViewModel`, screen, and reusable UI components
- `app/src/main/java/com/vectorlogic/blackjackcounter/ui/theme`
  - Theme, typography, and colors

## Notes
- Deck safety floor is enforced to avoid divide-by-zero behavior.
- True count is rounded to one decimal place for display.
- `CounterViewModel` is created via a factory in UI and tests.
- Educational/training use only.
