# Blackjack Counter (Android)

Blackjack card-counting practice app built with Kotlin + Jetpack Compose.

The app supports multiple counting systems, configurable blackjack table rules, and a full-screen strategy chart that updates based on your current true count and selected rules.

## Features
- Running Count, True Count, and Estimated Edge %
- Deck controls:
  - Total decks (1-8)
  - Decks remaining slider (`0.25` minimum)
  - Deck penetration
- Card input buttons (`A`, `2-10`, `T/J/Q/K`)
- Cards drawn / cards left / total cards tracking
- System-aware button colors (including strong tags like `+2`/`-2`)
- Menu actions:
  - Select Counting System
  - Table Rules
  - Help
  - Reset Count
  - Reset Shoe
  - Show/Hide Cheat Sheet
- Full-screen strategy chart (button below Reset Count)
  - Table format (no horizontal scrolling)
  - Back button
  - Actions updated by current true count
  - Rule-aware adjustments and count-based deviations (`*`)
- Hi-Lo style deviation guidance + split deviation guidance
- Ace side count only for systems that recommend it

## Counting Systems
- Hi-Lo
- KO
- ReKO
- Hi-Opt II (ace side count recommended)
- Omega II (ace side count recommended)
- Zen Count
- Wong Halves

All systems implement a shared `CountingSystem` interface for modular expansion.

## Table Rules Supported
- Dealer soft 17: `S17` / `H17`
- Dealer peek:
  - Peek on Ace and 10
  - Peek on Ace only (no peek on 10)
  - No peek (European style)
- Blackjack payout: `3:2` / `6:5`
- Double down:
  - Any 2 cards
  - 9-11 only
  - 10-11 only
- Double after split (DAS)
- Surrender: none / late / early
- Resplit aces
- Hit split aces
- Max split hands (2-6)
- 6-card Charlie auto win

## Estimated Edge %
`Edge %` is an in-app estimate derived from:
- current true count
- selected table rules

It is intended as a training indicator, not an exact simulator output.

## Tech Stack
- Kotlin
- Jetpack Compose
- Material 3
- MVVM (`ViewModel` + `StateFlow`)
- Local in-memory state only (no backend, no DB)

## Project Structure
- `app/src/main/java/com/yourname/blackjackcounter/data/counting`
  - counting system abstractions + implementations
- `app/src/main/java/com/yourname/blackjackcounter/domain/model`
  - app state, rules, shared calculation models
- `app/src/main/java/com/yourname/blackjackcounter/presentation/counter`
  - `CounterViewModel`, screen, and Compose components
- `app/src/main/java/com/yourname/blackjackcounter/ui/theme`
  - theme, colors, typography

## Requirements
- Android Studio (latest stable recommended)
- Android SDK (compileSdk 36)
- Min Android version: API 24
- JDK 11+

## Run (Android Studio)
1. Open project folder in Android Studio.
2. Let Gradle sync.
3. Select emulator/device.
4. Run `app`.

## Run / Build (CLI)
Windows:
- `.\gradlew.bat assembleDebug`
- `.\gradlew.bat testDebugUnitTest`

macOS/Linux:
- `./gradlew assembleDebug`
- `./gradlew testDebugUnitTest`

## Notes
- True count is clamped safely with minimum decks remaining to avoid divide-by-zero behavior.
- Strategy chart logic is rule-aware and count-aware, and intentionally optimized for fast training lookups.
