# Counting Edge: Card Counter (Android)

Counting Edge: Card Counter is a Jetpack Compose training app for live card-count practice.
It supports multiple counting systems, rule-aware strategy guidance, and a compact one-screen workflow.

## Current Release
- Version: `1.1.0`
- Version code: `10`
- Distribution: [GitHub Releases](https://github.com/vectorlogic-dev/card-counter/releases) + [blackjackcounter.app](https://blackjackcounter.app)
- Website: [blackjackcounter.app](https://blackjackcounter.app)

## 30-Second Setup Checklist
- Open `Menu` -> `Table Rules` and set table conditions first.
- Open `Menu` -> `Select Counting System`.
- Set `Total Decks` and `Decks Remaining`.
- Start tapping card ranks as they are dealt.
- Use `Bet Recommendation` units with your chosen base bet.

## How To Use (Top-to-Bottom Flow)
1. Open `Menu` -> `Table Rules` and set the table first.
   Rule choices affect strategy actions and the estimated edge model.
2. Open `Menu` -> `Select Counting System` and choose your system.
   Each entry shows its level, whether it is balanced, and whether it uses a true count.
   Switching mid-shoe recounts the cards already seen with the new tags and shows a short note
   with the old and new running count.
3. In `Deck Controls`, set:
   - `Total Decks` (segmented button: `1 / 2 / 4 / 6 / 8`)
   - `Decks Remaining` (slider, minimum `0.25`)
4. Tap card buttons as cards are dealt:
   - Row 1: `A`, `2`, `3`, `4`, `5`
   - Row 2: `6`, `7`, `8`, `9`, `10-K`
5. Read the live metrics:
   - `Running` count
   - `True` count (`N/A` for running-count-only systems)
   - `Edge %` (only shown when true count is supported)
6. Use `Bet Recommendation` as a training ramp (units), then map units to your own base bet.
7. Use extras as needed:
   - `Help` for system explanations, tag values, current table rules, and where to find the strategy tools
   - `Menu` -> `Show/Hide Cheat Sheet`
   - `Strategy Chart` button for full-screen basic strategy + deviations (system back returns to the counter)
   - `Undo` in the count pad header for correcting missed taps; the last few cards sit next to it
     and the newest (the one Undo removes) is highlighted
   - `Recent Cards` panel for a longer history
   - `Reset Count` to reset shoe depth and count state (asks to confirm when there are cards or shoe progress to lose)
   - `Menu` -> `Drill Mode` for timed running-count practice
   - For `KO` / `ReKO`, reset returns to the deck-specific initial running count (IRC)

## Bet Units Explained
The app recommends a unit size from the ramp `1-2-4-8-12`.

- A "unit" is your base bet amount.
  Example: if `1 unit = $10`, then `8 units = $80`.
- For true-count systems (balanced systems), thresholds are calibrated for Hi-Lo:
  - `< 1.0` -> `1` unit
  - `1.0 to <2.0` -> `2` units
  - `2.0 to <3.0` -> `4` units
  - `3.0 to <4.0` -> `8` units
  - `>= 4.0` -> `12` units

Level-2 systems produce true counts about 1.7-1.8x larger than Hi-Lo for the same shoe, so every
balanced system converts Hi-Lo thresholds to its own true count with a scale factor: the ratio of
the system's per-card tag standard deviation to Hi-Lo's. A threshold is the Hi-Lo value times the
scale, kept to one decimal (Omega II: Hi-Lo `+2` x 1.79 = `3.58` -> `3.6`). The same scale is used
for bet ramps, playing indices and the edge estimate, so a system bets and deviates at the same
Hi-Lo-equivalent count as Hi-Lo.

| System | Scale | Ramp thresholds (`2 / 4 / 8 / 12` units) |
|---|---|---|
| Hi-Lo | 1.00 | TC `1 / 2 / 3 / 4` |
| Wong Halves | 1.05 | TC `1.0 / 2.1 / 3.1 / 4.2` |
| Hi-Opt II | 1.67 | TC `1.7 / 3.3 / 5.0 / 6.7` |
| Omega II | 1.79 | TC `1.8 / 3.6 / 5.4 / 7.2` |
| Zen Count | 1.79 | TC `1.8 / 3.6 / 5.4 / 7.2` |

**Which true count is compared.** Bets, indices and the edge estimate all use the true count as
the app displays it, to one decimal. A step or play applies when that count is at or above the
threshold (or below it, for the negative "hit below" indices), so the numbers on the cheat sheet
and in the table above are exactly where things change. The count is never truncated toward zero:
a true count of `-0.9` is below `0`, so the index-0 plays (stand 16 vs 10, stand 12 vs 4,
surrender 15 vs 10) wait until the count reaches `0`. For whole-number thresholds such as Hi-Lo's,
this is the same as flooring the displayed true count, which is how most index references are
applied.
Truncation would treat `-0.9` as `0` and make those plays while the count is still negative.

For `KO` / `ReKO`, ramps are deck-aware and keyed from the system's `Key Count` and `Pivot`:
- `< Key Count` -> `1` unit
- `>= Key Count` -> `2` units
- `>= Pivot` -> `4` units
- `>= Pivot + 4` -> `8` units
- `>= Pivot + 8` -> `12` units

Unbalanced-system reset references are deck-specific:
- `KO` IRC formula: `4 - (4 * decks)`
- `KO` key counts follow *Knock-Out Blackjack*: `1D +2`, `2D +1`, `6D -4`, `8D -6`
  (other deck counts fall back to `2 - decks`); pivot is `+4`
- `ReKO` IRC formula: `1 - (3 * decks)`
- Example on `6` decks: `KO = -20`, `ReKO = -17`

This is a training aid, not bankroll or gambling advice.

## Counting Systems
- Hi-Lo
- KO
- ReKO
- Hi-Opt II (Ace side count recommended)
- Omega II (Ace side count recommended)
- Zen Count
- Wong Halves

All systems implement a shared `CountingSystem` interface.

All counting systems are available in the app at no extra cost.

## Current UI (Quick Overview)
- Header (floating, no card):
  - Current system name in gold
  - Info icon -> Help dialog
  - Menu icon -> Select Counting System, Table Rules, Reset Count, Show/Hide Cheat Sheet, Drill Mode
- Main stats row (3 cards with colored top accent strip):
  - Running Count
  - True Count (or `N/A` for unbalanced systems)
  - Estimated Edge %
- Bet Recommendation card:
  - Large unit number on the right; five pip dots on the left indicating position on the `1 -> 2 -> 4 -> 8 -> 12` ramp,
    with each step's unit count under its pip (the current step in bold)
- Strategy Chart button (full-screen chart)
- Deck controls:
  - Segmented button: `1 / 2 / 4 / 6 / 8` decks
  - Decks Remaining slider (`0.25` minimum)
  - Penetration %
  - Cards Drawn / Cards Left / Total Cards
  - Ace side count panel when selected system recommends it
- Card input (2 rows, each button shows rank + tag value):
  - Header row: the last few cards (newest highlighted) and an `Undo` button
  - `A`, `2`, `3`, `4`, `5`
  - `6`, `7`, `8`, `9`, `10-K`
  - Every tap gives a haptic tick, and the screen stays awake while the counter is visible
- Bottom actions:
  - Recent Cards panel with badge chips, newest highlighted (item count adapts for compact layouts; tens show as `10-K`)
  - Reset (with confirmation)
- Landscape (480 dp wide or more) and wide screens (600 dp and up) use two panes: header, counts,
  bet ramp and shoe on the left; count pad with Undo, Strategy Chart button and recent cards on the right

## Drill Mode
- Open from `Menu` -> `Drill Mode`; drills use the currently selected counting system
- Pick a length (½ deck / 1 deck / 1½ decks) and a speed (Slow / Medium / Fast)
- Cards are dealt from a shuffled real shoe, so a one-deck drill is an actual deck (Hi-Lo nets `0`)
- Each card is followed by a short blank, so the same rank twice in a row still reads as two cards;
  the counter shows the card's position (`1 / 26` for the first)
- Enter your final running count (the keyboard opens by itself and its Done key submits; a `±` toggle
  covers keyboards without a minus key) and get scored exact or off-by-N, plus cards/sec
- A drill in progress, including the answer step, survives rotation and isn't cancelled by Back or an
  accidental outside tap; `Close` still ends it

## Table Rules Supported
- Dealer soft 17: `S17` / `H17`
- Dealer peek: `ACE_AND_TEN`, `ACE_ONLY`, `NO_PEEK`
- Blackjack payout: `3:2` / `6:5`
- Double down: any two cards, 9 to 11 only, or 10 to 11 only
- Double after split (DAS)
- Surrender: none / late / early
- Resplit aces
- Hit split aces
- Max split hands: `2..6`
- 6-card Charlie auto win

## Strategy and Deviations
- Full-screen strategy chart in table form (hard/soft/pairs)
- Each action has its own fill as well as its letter: `H` dark slate, `S` gold, `D`/`Ds` green,
  `P` blue, `Rh`/`Rs`/`Rp` rose; a legend sits at the top of the chart
- Chart updates from current count context (when supported)
- Rule-aware action adjustments
- Deviation cells marked with `*` and an outline
- Indices are the Hi-Lo Illustrious 18 (plus 15 vs A stand at `+5`), converted to the selected
  system's true count with the scale factor above and kept to one decimal; the cheat sheet shows
  the converted values, which are exactly the ones the chart uses (e.g. Omega II 10 vs 10 double
  at `+7.2`, 12 vs 5 hit below `-3.6`). Indices are compared with the displayed true count; see
  "Which true count is compared" above
  - Positive indices: make the play at or above the index (e.g. stand 16 vs 10 at `TC 0+`)
  - Negative indices: hit below the index (12 vs 4 `0`, 12 vs 5 `-2`, 12 vs 6 `-1`, 13 vs 2 `-1`, 13 vs 3 `-2`)
- When surrender is offered, surrender takes precedence over stand indices (16 vs 9, 16 vs 10, 15 vs 10),
  and the Fab 4 surrender indices apply: 14 vs 10 `+3`, 15 vs 10 `0`, 15 vs 9 `+2`, 15 vs A `+1`
  (`-1` under H17, where basic strategy already surrenders it)
- H17 basic strategy (4-8 decks) adds: double 11 vs A, double soft 18 vs 2, double soft 19 vs 6, and with
  surrender, surrender 15, 17 and 8,8 vs A. 17 then gets its own chart row (`Rs`, surrender else stand),
  and 8,8 vs A shows `Rp` (surrender, else split)
- The cheat sheet lists every index for the selected system and highlights the plays that apply at the
  current count; doubles the table rules forbid show as `No double (rules)`, and plays basic strategy
  already makes at every count show as `(any TC)`
- Help points to the Strategy Chart and cheat sheet rather than repeating them

## Estimated Edge %
Estimated Edge % is computed from:
- current true count as displayed (if supported by the selected system), converted to a
  Hi-Lo-equivalent true count with the system's scale factor
- selected table rules
- total decks in shoe

Rule and deck adjustments are calibrated from Wizard of Odds rule-variation deltas (8-deck baseline model, then adjusted by selected deck count).

It is a simplified training estimate.

## Tech Stack
- Kotlin
- Jetpack Compose
- Material 3
- MVVM (`ViewModel` + `StateFlow`)
- Local state only (no backend, no database)
- Session state (running count, shoe depth, recent cards, selected system, table rules) is saved to on-device SharedPreferences and restored on the next launch; it is excluded from cloud backup and device transfer

## Requirements
- Android Studio (latest stable recommended)
- JDK 17+ (Gradle 9; the build daemon runs on JDK 21 and is provisioned automatically)
- `compileSdk` 36
- `minSdk` 24
- Package/application ID: `com.vectorlogic.blackjackcounter`

## Run (Android Studio)
1. Open this folder in Android Studio.
2. Wait for Gradle sync.
3. Select an emulator or physical device.
4. Run the `app` configuration.

## Build / Test (CLI)
Windows:
- `./gradlew.bat assembleDebug`
- `./gradlew.bat testDebugUnitTest`

macOS/Linux:
- `./gradlew assembleDebug`
- `./gradlew testDebugUnitTest`

## Release Checklist
- Verify tests and lint pass: `./gradlew testDebugUnitTest lintRelease`
- Publish: `scripts/publish-release.sh <version>` — see the Download section.

## Privacy Policy
- Privacy policy source: [docs/privacy-policy.md](docs/privacy-policy.md); the hosted copy is at [blackjackcounter.app/privacy-policy.html](https://blackjackcounter.app/privacy-policy.html).
- The app stores session state locally only (SharedPreferences on-device). Nothing is transmitted off-device by the app itself.

## Project Structure
- `app/src/main/java/com/vectorlogic/blackjackcounter/data/counting`
  - Counting system interface + implementations
- `app/src/main/java/com/vectorlogic/blackjackcounter/domain/model`
  - App state, card/rule models, and core calculations
- `app/src/main/java/com/vectorlogic/blackjackcounter/presentation/counter`
  - `CounterViewModel`, screen, and reusable UI components
- `app/src/main/java/com/vectorlogic/blackjackcounter/ui/theme`
  - Theme, typography, and colors

## Notes
- Deck safety floor is enforced to avoid divide-by-zero behavior.
- True count is rounded to one decimal place, and that displayed value is what bets, indices and the edge estimate use.
- `CounterViewModel` is created via a factory in UI and tests.
- Educational/training use only.

## Download

Signed release APKs are published to the [GitHub Releases page](https://github.com/vectorlogic-dev/card-counter/releases). To install on Android:

1. Download the `.apk` file onto your device.
2. Allow installation from unknown sources for your browser/file manager (one-time Android prompt).
3. Open the file and tap **Install**.

Installing a new release over an older one preserves app data (same package, same signing key).

### Publishing a new release (maintainers)

Releases are built and signed locally, then uploaded to GitHub — no signing key ever leaves the maintainer's machine. Requires a `keystore.properties` file at the repo root (gitignored) pointing at the release keystore:

```
storeFile=/absolute/path/to/release.jks
storePassword=...
keyAlias=...
keyPassword=...
```

Then run:

```bash
scripts/publish-release.sh 1.0.0           # publish v1.0.0
scripts/publish-release.sh 1.0.0 --draft   # stage without publishing
```

Before running it, bump `versionName` and `versionCode` in `app/build.gradle.kts` and commit. The script stops before building if the version argument doesn't match `versionName`, if `versionCode` isn't higher than in the previous release tag (sideloaded installs couldn't upgrade), or if `gh` isn't authenticated.

It then runs `./gradlew clean testDebugUnitTest` and `./gradlew assembleRelease bundleRelease`, computes the APK SHA-256, opens release notes in `$EDITOR`, tags and pushes `v1.0.0`, and uploads the APK (and the AAB, if one was built) to the release.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for development setup, build commands, and the pull-request process. Issues and feature requests are welcome via GitHub Issues.

## License

Released under the [Apache License 2.0](LICENSE).

Educational/training use only. This app does not facilitate gambling and is not affiliated with any casino or gaming operator.
