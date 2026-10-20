# UI/UX Audit — Counting Edge

Audit run: 2026-05-06. Covers all files under `presentation/counter`, `ui/theme`.
Already-applied changes are marked [DONE].

---

## Priority 1 — Functional / User-Facing

### 1. Count numbers use proportional font — layout shifts on sign change [OPEN]
**File:** `ui/theme/Type.kt`
**Issue:** `displayMedium` and `headlineMedium` (used for count values) use `FontFamily.SansSerif`.
Proportional fonts give different widths to "+12" vs "-1", causing the card width or text to
visually shift on every tap.
**Fix:** Add `FontFeatureSettings("tnum")` to display/headline styles used for numeric values,
or switch to a condensed/tabular font like Roboto Mono for count display text.
```kotlin
displayMedium = TextStyle(
    ...
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    // add to TextStyle: textFeatureSettings = "tnum"  (not yet in stable API — use workaround)
)
```
Workaround: constrain the `CountDisplayCard` value `Text` with `Modifier.widthIn(min = 72.dp)` to
pin the card width regardless of sign width.

---

### 2. "N/A" values rendered in full-brightness `onSurface` color [OPEN]
**File:** `presentation/counter/CounterScreenContent.kt` lines 130–134, 145–149
**Issue:** When a system doesn't support true count or edge, "N/A" is shown using
`MaterialTheme.colorScheme.onSurface` — the same color as active live values. This makes
"N/A" look like a live data point rather than a disabled/unavailable state.
**Fix:** Use `MaterialTheme.colorScheme.onSurfaceVariant` for "N/A" values:
```kotlin
valueColor = if (state.selectedSystemUsesTrueCount) {
    countColor(trueCountValue)
} else {
    MaterialTheme.colorScheme.onSurfaceVariant  // was: onSurface
}
```
Apply the same fix to the Edge % card.

---

### 3. Missing contentDescription on all interactive elements [OPEN]
**Files:** `CountButtonsRow.kt`, `StrategyChartButton.kt`, `ResetButton.kt`, `DeckControlCard.kt`
**Issue:** TalkBack users hear "Button" for every card input button with no card context.
The deck slider and segmented buttons also have no accessibility labels.
**Fixes:**
- `CardInputButton`: pass `Modifier.semantics { contentDescription = "Card $label" }` to the Button
- `StrategyChartButton`: add `contentDescription = "Open strategy chart"`
- `ResetButton`: add `contentDescription = "Reset count and shoe"`
- Deck segmented buttons: wrap each in `Modifier.semantics { contentDescription = "$option decks" }`
- Slider: add `Modifier.semantics { contentDescription = "Decks remaining: $formatted" }`

---

### 4. StrategyChartFullScreen — fake centering with placeholder Box [OPEN]
**File:** `StrategyChartDialogContent.kt` lines 56–58
**Issue:** The header Row uses a `TextButton("Back")` on the left and an invisible
`Box(modifier = Modifier.padding(end = 8.dp))` as a spacer on the right to fake-center
the title. This is a layout hack.
**Fix:** Replace with a proper `CenterAlignedTopAppBar` or use `Modifier.weight(1f)` on
the title with `Alignment.CenterHorizontally`.

---

## Priority 2 — Polish / Clarity

### 5. `formatDecksRemaining` shows 2 unnecessary decimal places [OPEN]
**File:** `DeckControlCard.kt` line 247
**Issue:** `"%.2f"` format renders "2.00 decks" when "2.0" is sufficient. The slider snaps
to 0.25 steps, so one decimal place is the highest useful precision.
**Fix:**
```kotlin
private fun formatDecksRemaining(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        String.format(Locale.US, "%.1f", value)
    } else {
        String.format(Locale.US, "%.2f", value)
    }
}
```
Or simply always use `"%.2f"` but only for the slider label — use `"%.1f"` for the hero
display above the slider.

---

### 6. "units live" label in BetRecommendationCard is awkward [OPEN]
**File:** `BetRecommendationCard.kt` line 106
**Issue:** The label "units live" under the unit count reads strangely.
**Fix:** Change to "bet units" or just "units".

---

### 7. BetRecommendationCard "1 2 4 8 12" text label is redundant [OPEN]
**File:** `BetRecommendationCard.kt` line 88
**Issue:** The text "1 2 4 8 12" appears immediately next to the dot ramp indicators.
The dots already represent those values. The text adds visual noise with no information gain.
**Fix:** Remove the `Text("1 2 4 8 12")` or replace with a compact "ramp" label like
"×1  ×2  ×4  ×8  ×12" shown only at the dot positions.

---

### 8. RecentCardsCard empty state is generic [OPEN]
**File:** `RecentCardsCard.kt` line 67
**Issue:** "No cards yet" tells the user nothing about what to do.
**Fix:** Change to `"Tap cards above to begin tracking"`.

---

### 9. SystemSelector — "Level X" unexplained [OPEN]
**File:** `SystemSelector.kt` line 55
**Issue:** "Level 1" / "Level 2" / "Level 3" is shown with no legend. New users don't know
higher level = more complex.
**Fix:** Add a one-line descriptor per level, or add a single footnote at the bottom of the
list: `"Level 1 = simplest, Level 3 = most complex"`.

---

### 10. HelpHeaderCard has four near-duplicate `bodySmall` lines [OPEN]
**File:** `HelpDialogContent.kt` lines 68–87
**Issue:** Four separate `Text` composables in `HelpHeaderCard` repeat the same idea —
"here's how to read this help section." Could be condensed to one or two sentences.
**Fix:** Replace the four texts with:
```kotlin
Text(
    text = "Each system shows level, balance, and tag values. " +
           "The strategy chart is baseline — use Table Rules for context.",
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant
)
```

---

## Priority 3 — Structural Notes

### 11. App is hardcoded dark-only [NOTE]
**File:** `Theme.kt` line 67
`BlackjackCounterTheme(darkTheme: Boolean = true)` — the light scheme exists in the theme
but is never activated. The `MainActivity` does not call `isSystemInDarkTheme()`.
This is likely intentional (casino use = dark environment) but should be documented.
If light mode support is ever added, wire `isSystemInDarkTheme()` in `MainActivity`.

---

### 12. `TableRulesDialogContent` Switch has no semantic label [OPEN]
**File:** `TableRulesDialogContent.kt`
The `Switch` in `RulesSwitchCard` has no `contentDescription`. TalkBack reads the
adjacent `Text` label through the layout, but this is fragile.
**Fix:** Add `Modifier.semantics { contentDescription = "$title: ${if (checked) "on" else "off"}" }`
to the `Switch`.

---

## Already Applied

- [DONE] Running count displayed as hero (`hero = true`) in `CounterScreenContent.kt`
- [DONE] `BetRecommendationCard` moved above `StrategyChartButton` (count → bet → reference order)
- [DONE] `StrategyChartButton` changed from filled `Button` to `OutlinedButton`
