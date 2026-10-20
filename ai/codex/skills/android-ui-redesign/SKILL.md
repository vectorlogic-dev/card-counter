---
name: android-ui-redesign
description: Redesign this Android app's Jetpack Compose UI for production-grade visual quality, stronger UX, and a more premium Play Store presentation. Use when the app should look much better, more modern, more intentional, or more polished than a simple spacing/layout tweak, especially for major work in presentation/counter and ui/theme.
---

# Android UI Redesign

## Overview

Redesign screens as products, not as piles of composables.
Prioritize a strong visual system, fast gameplay interaction, and a Play-quality first impression.

This app is a rapid-input blackjack training tool.
Optimize for:
- one-glance count readability
- fast one-thumb card entry
- compact but premium-looking information density
- a cohesive casino/training visual language rather than generic Material defaults

## Workflow

1. Identify the primary user loop first:
- read count state
- enter cards quickly
- adjust shoe state
- recover from mistakes
- open deeper help only when needed
2. Redesign hierarchy before details:
- make the most important number visually dominant
- group secondary controls into calmer surfaces
- reduce repeated labels and weak visual noise
3. Establish a visual direction:
- define a clearer surface system in `ui/theme`
- use contrast, accent color, radius, and spacing intentionally
- avoid plain card stacks that all feel identical
4. Tighten interaction quality:
- keep high-frequency buttons large and visually distinct
- keep dangerous or destructive actions quieter than gameplay actions
- make dialogs and menus feel deliberate, not placeholder
5. Finish with a density pass using `android-gui-polish`.
6. Verify with `android-verify-workflow`.

## Repo Targets

- `presentation/counter/CounterScreen.kt`
- `presentation/counter/CounterScreenContent.kt`
- `presentation/counter/components/*.kt`
- `ui/theme/Color.kt`
- `ui/theme/Theme.kt`
- `ui/theme/Type.kt`

## Guardrails

- Do not move game logic out of ViewModel/domain just to simplify a visual redesign.
- Do not make the screen pretty at the cost of gameplay speed.
- Prefer a small number of strong surface styles over many slightly different card treatments.
- Keep copy concise and blackjack-specific.
- Preserve compact usability on smaller phones.

## Redesign Heuristics

Read `references/redesign-rules.md` before making substantial UI changes.

## Sequence

- For major visual upgrades: `android-ui-redesign` then `android-gui-polish` then `android-verify-workflow`.
- For small density tweaks only: use `android-gui-polish` directly.
