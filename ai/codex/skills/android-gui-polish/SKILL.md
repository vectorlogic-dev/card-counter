---
name: android-gui-polish
description: Refine and compact this app's Jetpack Compose UI for faster gameplay and clearer readability. Use when adjusting spacing, hierarchy, typography, card/button sizes, one-screen fit, and responsive behavior in files under presentation/counter and ui/theme.
---

# Android Gui Polish

## Overview

Make targeted layout and style improvements without changing core blackjack logic.
Prioritize one-glance visibility of running/true count, deck controls, and card input buttons.

## Workflow

1. Confirm the required interaction flow:
- Player sees all key data at once.
- Player can tap card buttons with minimal travel.
- Critical values remain readable on smaller phones.
2. Reduce visual overhead before adding new UI:
- Remove optional sections first.
- Merge cards/labels where possible.
- Tighten vertical spacing and control heights.
3. Validate touch and readability:
- Keep tap targets practical for mobile.
- Keep high contrast for count values.
- Preserve Material 3 consistency.
4. Verify build after UI edits using `$android-verify-workflow`.

## Quick UI Check

Run this quick check before and after edits:

1. Can the player see running count, true count, deck controls, penetration, and card buttons at once?
2. Are critical numbers readable at arm's length?
3. Are card buttons easy to hit repeatedly with one thumb?
4. Does visual emphasis point to counts first, then controls?
5. Does the screen still compile and run without clipping on smaller phones?

## Guardrails

- Do not move business rules from ViewModel/domain into composables.
- Keep text and control naming blackjack-focused and concise.
- Prefer stateless components with parameters for display-only values.
- Keep dark theme visual language in sync with `ui/theme`.

## Typical Targets

- `presentation/counter/CounterScreen.kt`
- `presentation/counter/components/*.kt`
- `ui/theme/Type.kt`
- `ui/theme/Color.kt`

## Resource

Read `references/gui-rules.md` when choosing spacing, sizing, and information density tradeoffs.
