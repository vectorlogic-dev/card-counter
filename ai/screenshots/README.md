# Screenshots

Captured from the debug build on a Pixel 8 emulator (1080×2400). All taken in dark mode (the app currently ships dark-only). Use these for the website, store listing, and READMEs.

| File | Shows |
|------|-------|
| `01-main-counter.png` | Hi-Lo, fresh state — hero shot showing the one-screen layout, 6-deck shoe. |
| `02-counter-active.png` | Hi-Lo mid-shoe: Running +15, True +2.6, Edge +0.8%, bet ramp at 4 units. Best "it works" shot. |
| `03-menu.png` | Overflow menu (Select Counting System, Table Rules, Reset Count, Show Cheat Sheet, Drill Mode). |
| `04-strategy-table.png` | Full-screen Strategy Chart. TC 2.6 deviations highlighted with asterisks. |
| `05-help.png` | Help dialog — Counting Systems Reference + Current Table Rules + per-system blurb. Doubles as a rules reference shot. |
| `06-system-picker.png` | All seven systems in one dialog, with levels and ace-side-count badges. |
| `07-system-KO.png` | KO on 6 decks — unbalanced system reset to IRC −20. True/Edge show N/A, bet ramp drives off running count. |
| `08-system-ReKO.png` | ReKO on 6 decks — IRC −17. |
| `09-system-HiOptII.png` | Hi-Opt II — Ace side count badge, A-Seen/A-Exp columns, "Ace outlook" line, level-2 tag values (10-K = −2). |
| `10-system-OmegaII.png` | Omega II — another level-2 Ace-side-count system. |
| `11-system-Zen.png` | Zen Count — level-2 balanced. |
| `12-system-WongHalves.png` | Wong Halves — level-3 balanced with fractional tags (+0.5, +1.5, −0.5). |

## Suggested website pairings

- **Hero band:** `02-counter-active.png` (visibly working).
- **Feature grid:** `06-system-picker.png` + `04-strategy-table.png` + `05-help.png`.
- **System depth row:** `07`, `09`, `12` (three different flavors — unbalanced, Ace-side, fractional).
- **Avoid as hero:** `01` (zero-state, less visually interesting than `02`).

## Re-capturing

The emulator-only install flow:

```bash
~/Library/Android/sdk/emulator/emulator -avd Pixel_8 -no-snapshot-save -no-boot-anim -no-audio &
adb wait-for-device
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell monkey -p com.vectorlogic.blackjackcounter.debug -c android.intent.category.LAUNCHER 1
# Interact as needed, then:
adb exec-out screencap -p > ai/screenshots/<name>.png
```

If the Google "Sign in with ease" nag appears when switching apps, tap SKIP (bottom-left) or `adb shell input tap 100 1870` and relaunch the app.
