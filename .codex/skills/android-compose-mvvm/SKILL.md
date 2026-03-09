---
name: android-compose-mvvm
description: Build and refactor Android app features in this repository using Kotlin, Jetpack Compose, Material 3, and MVVM. Use when adding or modifying screens, composables, ViewModels, domain models, or counting-system logic under app/src/main/java/com/yourname/blackjackcounter, and when code should keep business logic out of composables.
---

# Android Compose Mvvm

## Overview

Implement changes using the repository's current layering and naming style.
Keep UI declarative, keep state in `CounterViewModel`-style classes, and keep domain rules out of composables.

## Workflow

1. Identify the target layer before editing:
- `domain/model`: pure data/state types.
- `data/counting`: counting system abstraction and implementations.
- `presentation/counter`: ViewModel and UI components.
- `ui/theme`: app visual system.
2. Add or update models and interfaces first.
3. Implement ViewModel state/events next.
4. Wire composables to observe state and dispatch actions.
5. Run verification with `android-verify-workflow` before handoff.

## Guardrails

- Keep business logic in ViewModel/domain, not in composables.
- Use `StateFlow` or `mutableStateOf` consistently within a screen feature.
- Keep file/package names aligned with existing structure.
- Prefer small composables in `presentation/counter/components`.
- Preserve deck constraints:
  - total decks: `1..8`
  - decks remaining: minimum `0.25`, maximum `totalDecks`
- Preserve true-count safety:
  - no division by zero
  - round displayed value to one decimal place

## Common Task Routing

- Add a counting system:
  - Add `data/counting/<SystemName>.kt` implementing `CountingSystem`.
  - Register in `CounterViewModel`.
  - Keep UI exposure explicit (v1 can keep only Hi-Lo exposed).
- Add screen-level state:
  - Extend `CounterState`.
  - Add ViewModel events and state updates.
  - Bind UI via `collectAsState()`.
- Add reusable UI:
  - Create/modify a file in `presentation/counter/components`.
  - Keep parameters simple and stateless.

## Resource

Read `references/repo-architecture.md` for the current package map and expected file locations.
