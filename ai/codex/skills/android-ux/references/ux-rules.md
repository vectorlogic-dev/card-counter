# UX Rules For This Repo

## 1. Information hierarchy drives visual hierarchy
The most important value (running count) must be the most visually dominant element on screen.
Use `hero = true` on `CountDisplayCard` for the running count. Never render the primary count value
the same visual weight as secondary metrics.

## 2. Action hierarchy must match intent hierarchy
- Card input buttons: highest visual weight (filled, colored, large tap target)
- Navigation / reference (strategy chart): outlined, never filled
- Destructive actions (reset): outlined, positioned last, never near card buttons
- Dialogs and menus: launched from a quiet icon or menu, never an oversized primary button

## 3. Information sequencing follows the decision chain
Top to bottom: Running count → True count + Edge → Bet recommendation → Strategy reference → Card input → Shoe → History → Reset.
Never place a navigation action (Strategy Chart) between derived count values and bet sizing.

## 4. Reversibility
Every card tap must have a one-tap undo. The Undo control should remain visible at all times
when cards have been entered — never hidden behind a menu.

## 5. Feedback clarity
State changes (count increment, bet size jump, deck change) should be immediately visible.
Do not animate state changes in a way that adds lag to the read cycle.

## 6. Touch targets
- Card input buttons: minimum 52dp height. Prefer 56dp on non-compact layouts.
- All other interactive controls: minimum 48dp (Material 3 minimum).
- Do not place two interactive controls closer than 4dp edge-to-edge.

## 7. Labels and copy
- Prefer concise blackjack-native labels: "RC", "TC", "Pen", "A side", "BJ pays"
- Remove labels that restate the card title or repeat the unit already in the value
- "N/A" is acceptable for metrics that are genuinely unavailable; use muted color, not error color

## 8. Accessibility
- All interactive controls must have a non-empty `contentDescription`
- Count values must meet WCAG AA contrast (4.5:1) against their card background
- Positive counts (green) and negative counts (red) must differ by more than color alone
  (use + / − prefix, not just hue, for color-blind users)

## 9. Onboarding
- First-time users can infer card values from button tag labels (+1, −1, etc.)
- Help is one tap away from any state via the Info icon
- Do not add tutorial overlays unless specifically requested
