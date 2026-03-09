# Blackjack Counter (Android)

A beginner-friendly blackjack card counting practice app built with Kotlin, Jetpack Compose, Material 3, and MVVM.

## Tech Stack
- Kotlin
- Jetpack Compose
- Material 3
- MVVM (`ViewModel` + `StateFlow`)
- Local in-memory state only

## Version 1 Features
- Running Count display
- True Count display (`runningCount / decksRemaining`)
- Total decks selector (1 to 8)
- Decks remaining slider (minimum `0.25`)
- Count buttons: `+1`, `0`, `-1`
- Reset button
- Toggleable Hi-Lo cheat sheet
- Dark casino-inspired UI

## Architecture
- `data/counting`: counting system abstractions and implementations
- `domain/model`: app state and shared domain models
- `presentation/counter`: ViewModel + screen + UI components
- `ui/theme`: Material 3 colors, typography, and app theme

The counting systems are modular through the `CountingSystem` interface, so new systems can be added without changing existing core logic.

## Counting Systems Included in Code
- Hi-Lo (exposed in v1 UI)
- KO
- Omega II
- Zen Count
- Wong Halves

## True Count Rules
- Uses `trueCount = runningCount / decksRemaining`
- `decksRemaining` is clamped to at least `0.25` to avoid divide-by-zero
- True count is rounded to 1 decimal place

## Future Refactor Path for Training Mode
1. Add a `TrainerViewModel` in `presentation/trainer`.
2. Reuse `CountingSystem` + `CardRank` to evaluate user-selected cards.
3. Add training-specific domain models:
   - `TrainingRound`
   - `TrainingResult`
   - `TrainingSettings`
4. Keep `CounterViewModel` focused on live counting controls.
5. Add local persistence later (`DataStore`/Room) only when needed.

