# Counting Edge: Card Counter — Website Copy

Source material for the marketing site at `/Users/argie/repos/card-counter-website`. Rewrite freely to match the site's voice; this file is the canonical fact sheet.

---

## One-liner

The fastest way to train live card-count practice on your phone — multiple systems, rule-aware strategy guidance, and a single-screen workflow built for the table.

## Short description (store / hero)

Counting Edge is a Jetpack Compose Android app for serious blackjack students. Pick a counting system, dial in the shoe and the table rules, and tap cards as they're dealt. The app keeps a live running count, true count, estimated edge, and a bet-ramp recommendation — all on one screen, with no login, no ads, and no gambling features.

## Longer description (features page)

Counting Edge turns the kitchen-table drill into muscle memory. It's a training aid built around a one-screen workflow: your running count, true count, estimated edge, and bet recommendation are visible at all times, and every tap on a card button updates the model instantly. Rule choices in the Table Rules sheet feed back into basic-strategy deviations and the edge estimate, so the number you see reflects the game you're actually playing — not a generic simulator.

The app ships with seven counting systems — Hi-Lo, KO, ReKO, Hi-Opt II, Omega II, Zen Count, and Wong Halves — all free, all behind a single, shared interface. Advanced systems with Ace side counts expose a dedicated side-count panel. Unbalanced systems (KO, ReKO) reset to a deck-aware initial running count (IRC), so a reset in the middle of a six-deck shoe does the right thing automatically. Recent cards and an Undo button cover inevitable mistaps.

The bet recommendation uses a `1-2-4-8-12` ramp with thresholds appropriate to the chosen system — true-count thresholds for balanced systems, Key-Count / Pivot thresholds for unbalanced ones. The number is a unit multiplier, so you map it to your own base bet (one unit = whatever you decide one unit is worth).

A full-screen Strategy Chart shows basic strategy and the common count-based deviations side by side, and a toggleable Cheat Sheet overlays frequently-missed plays over the counter screen.

Everything runs locally on the device. The app does not create accounts, does not transmit gameplay telemetry, and does not persist session state across launches — the shoe resets when you close the app.

## Key features (bullet list)

- **Seven counting systems** — Hi-Lo, KO, ReKO, Hi-Opt II, Omega II, Zen Count, Wong Halves. All free, all unlocked.
- **Live metrics** — Running count, true count (balanced systems), estimated edge %, penetration %, and shoe depth.
- **Rule-aware strategy** — Table Rules sheet (dealer S17/H17, DAS, surrender, number of splits) feeds basic-strategy deviations and the edge model.
- **Bet ramp recommendations** — `1-2-4-8-12` unit ramp with thresholds tuned per system.
- **Deck controls** — 1/2/4/6/8-deck shoes, decks-remaining slider with a 0.25 floor to avoid divide-by-zero.
- **Ace side count** — Dedicated panel for systems that benefit (Hi-Opt II, Omega II).
- **Deck-aware resets for unbalanced systems** — KO and ReKO reset to the correct IRC for the chosen shoe.
- **Undo + recent cards panel** — Recover from mistaps without resetting the shoe.
- **Full-screen Strategy Chart** — Basic strategy and count-based deviations in one place.
- **Cheat Sheet overlay** — Toggle common deviations over the counter screen.
- **Session resume** — Running count, shoe depth, selected system, and table rules survive app restarts. All stored locally.
- **Drill Mode** — Timed running-count practice. Pick length (26/52/78 cards) and speed, watch cards flash, enter the final count, scored exact / off-by-N.
- **Open source** — Apache 2.0, source at [github.com/vectorlogic-dev/card-counter](https://github.com/vectorlogic-dev/card-counter). Anyone can audit, fork, or contribute.
- **No login, no ads, no telemetry** — Everything runs on the device. Nothing is sent anywhere.

## Who it's for

- **Blackjack students** working through a counting course and want a fast drill tool.
- **Advantage players** who want a pocket reference for less-common systems (Hi-Opt II, Omega II, Wong Halves) with the right side-count affordances.
- **Dealers and pit staff** training to recognize count-based play.

Not a gambling app. The app does not take bets, does not connect to any casino or gaming operator, does not simulate a game, and does not pay out anything. It's a counter and a strategy reference.

## What makes it different

- **One-screen workflow.** Metrics, controls, and card input all visible at once. No tab switching, no modal dance.
- **System breadth.** Most free counters stop at Hi-Lo. Counting Edge covers four balanced systems and two unbalanced ones, with the side counts and IRC handling they each need.
- **Rule-aware math.** Edge and strategy deviations change when you change the table rules.
- **Fully offline.** No account, no sync, no network.

## Technical facts

- **Platform:** Android, minimum API 24 (Android 7.0), target API 36.
- **Stack:** Kotlin, Jetpack Compose, MVVM.
- **Open source:** Apache 2.0, source at [github.com/vectorlogic-dev/card-counter](https://github.com/vectorlogic-dev/card-counter).
- **Distribution:** Direct APK download from [blackjackcounter.app](https://blackjackcounter.app) and [GitHub Releases](https://github.com/vectorlogic-dev/card-counter/releases). Not on Google Play.
- **Privacy:** No analytics, no accounts, no in-app ads. Session state is stored locally on the device only.

## Download CTAs

- **Primary:** Download APK from [blackjackcounter.app](https://blackjackcounter.app).
- **Mirror:** [github.com/vectorlogic-dev/card-counter/releases](https://github.com/vectorlogic-dev/card-counter/releases).
- **Source:** [github.com/vectorlogic-dev/card-counter](https://github.com/vectorlogic-dev/card-counter)

The site should pull the latest APK URL from the GitHub Releases API so the Download button never needs a manual bump — see [`docs/website/website-reference.md`](../docs/website/website-reference.md) §14 for the exact endpoint and fallback.

## Taglines (pick one)

- Train the count. Keep the edge.
- Seven systems. One screen. Zero noise.
- The counter that respects the table rules.
- Offline, open source, no bets, no ads.

## Legal / tone notes for the site

- **Never** frame the app as "how to beat the casino" or make earnings claims.
- Position as training / educational.
- Note that card counting is legal in most jurisdictions but casinos may refuse service.
- Link to the privacy policy at `/docs/privacy-policy.md` in the main repo (or wherever the website hosts it).
- Mention "Apache 2.0" and link to the GitHub repo prominently — being open source is a trust signal for this category.

## Screenshots

See `ai/screenshots/` for raw device captures. Suggested order for the website:
1. **Hero:** Main counter screen with a non-zero running count and visible bet recommendation.
2. **Table Rules sheet** — showing the dealer / DAS / surrender toggles.
3. **System picker** — showing all seven systems in one list.
4. **Strategy Chart (full screen)** — basic strategy + deviations.
5. **Help / System explainer** — the per-system help dialog.
