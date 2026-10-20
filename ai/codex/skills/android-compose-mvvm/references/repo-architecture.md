## Package Map

- `app/src/main/java/com/yourname/blackjackcounter/data/counting`
- `app/src/main/java/com/yourname/blackjackcounter/domain/model`
- `app/src/main/java/com/yourname/blackjackcounter/presentation/counter`
- `app/src/main/java/com/yourname/blackjackcounter/presentation/counter/components`
- `app/src/main/java/com/yourname/blackjackcounter/ui/theme`

## Architecture Rules

- Put count-system behavior behind `CountingSystem`.
- Keep mutable app state in ViewModel (`StateFlow`).
- Keep composables mostly stateless and parameter-driven.
- Keep app-specific calculations in ViewModel/domain.
- Avoid backend, auth, or storage additions unless requested.

## Current Product Rules

- Running count is incremented with `+1`, `0`, `-1` buttons.
- True count uses `runningCount / decksRemaining`.
- Clamp decks remaining to at least `0.25`.
- Round true count to one decimal place.
- Keep total decks in range `1..8`.
