---
name: android-ux
description: Improve the interaction quality, feedback, accessibility, and cognitive load of this Android Jetpack Compose app. Use when the problem is how the app feels to use — not just how it looks (use android-ui-redesign) or how densely it packs information (use android-gui-polish). Covers: touch target quality, visual feedback, action hierarchy, information sequencing, onboarding clarity, and error recovery.
---

# Android UX

## Overview

This skill targets how the app feels and behaves, not just how it looks.

This app is a high-speed blackjack training tool used under real table pressure.
UX quality here means:
- the right information is in the right place at the right time
- interactions are forgiving and reversible
- the hierarchy of actions matches the hierarchy of player intent
- visual feedback is instant and unambiguous
- low-frequency actions don't crowd high-frequency ones

## UX Improvement Workflow

1. **Map the player's primary loop first:**
   - Deal card → tap card button → observe count change → adjust bet
   - Every element not in that loop is secondary.
2. **Check information sequencing:**
   - Does the screen read top-to-bottom in the order a player actually thinks?
   - Count first → derived values (true count, edge) → bet sizing → action inputs
3. **Audit action hierarchy:**
   - Filled buttons = primary gameplay actions (card input)
   - Outlined buttons = utility/navigation (strategy chart, reset)
   - Destructive or dangerous actions should be visually quieter than gameplay actions
4. **Evaluate feedback quality:**
   - Is every tap confirmed visually or haptically?
   - Does a wrong count update have a clear, one-tap undo path?
   - Are error or warning states distinct without being alarming?
5. **Check touch target quality:**
   - Min 48dp hit target for all interactive elements
   - Card input buttons should be at least 52dp tall
   - High-frequency buttons should be larger than low-frequency ones
6. **Check cognitive load:**
   - Remove labels that restate what the value already communicates
   - Abbreviate without ambiguity (blackjack-trained users understand "Pen", "TC", "RC")
   - Avoid showing N/A for a metric that has a visible off-state (use dim/muted instead)
7. Verify with `android-verify-workflow`.

## Action Hierarchy Rules

| Button type        | Use for                        | Style          |
|--------------------|-------------------------------|----------------|
| `Button` (filled)  | Primary gameplay input        | Filled, colored |
| `OutlinedButton`   | Utility / reference / reset   | Outlined        |
| `TextButton`       | Inline confirmations / dialogs | No container   |
| `IconButton`       | Header controls (help, menu)  | No container   |

Never use a filled `Button` for navigation or reference actions (strategy chart, help, reset). That visual weight competes with card input buttons and trains the eye wrong.

## Information Sequencing

Top-to-bottom reading order should match the blackjack decision chain:

1. Running count (hero — most read value)
2. True count + Edge % (derived, side by side)
3. Bet sizing (immediate action output)
4. Strategy chart access (reference lookup — lower priority than bet sizing)
5. Card input pad (primary interaction zone)
6. Shoe controls (session setup, adjusted infrequently)
7. Recent cards (audit trail)
8. Reset (destructive, should be last and quiet)

## Cognitive Load Heuristics

Read `references/ux-rules.md` before making interaction changes.

## Sequence

- For UX interaction / flow / feedback improvements: use `android-ux`
- For visual system / color / typography overhauls: use `android-ui-redesign` then `android-gui-polish`
- For density and spacing only: use `android-gui-polish`
- Always finish with: `android-verify-workflow`
