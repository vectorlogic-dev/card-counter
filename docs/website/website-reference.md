# Counting Edge — Website Reference

Authoritative source document for building the marketing website at **blackjackcounter.app**.
Keep this in sync with the GitHub release metadata and app strings. Structured data lives in [`website-content.json`](./website-content.json).

---

## 1. Identity

| Field | Value |
|-------|-------|
| Full name | Counting Edge: Card Counter |
| Short name | Counting Edge |
| Package | `com.vectorlogic.blackjackcounter` |
| Developer | vectorlogic |
| Platform | Android (direct APK, not Google Play) |
| Source | [github.com/vectorlogic-dev/card-counter](https://github.com/vectorlogic-dev/card-counter) · Apache 2.0 |
| Latest release | [v1.0.0](https://github.com/vectorlogic-dev/card-counter/releases/latest) |
| Category | Card Games / Casino / Training Tools |

---

## 2. Tagline Options

- **Primary:** "Count every card. Win every edge."
- **Alt A:** "The serious card counter's training tool."
- **Alt B:** "Seven counting systems. One screen. No guessing."
- **Short / badge:** "Live card counting. Real table rules."

Pick one for the hero. Use the others as subheads or section intros.

---

## 3. One-Paragraph Description

> Counting Edge is a live blackjack card-counting trainer for Android.
> Tap card ranks as they fall, and the app tracks your running count,
> true count, player edge, and bet recommendation in real time.
> Seven professional counting systems — from beginner-friendly Hi-Lo
> to advanced Wong Halves — all rule-aware and calibrated to the exact
> table conditions you set. One screen. No subscriptions. No ads.

---

## 4. Feature List

Use these as bullet points, feature cards, or section headers on the site.

### Core Counting
- Live running count updated on every card tap
- True count calculation (running ÷ decks remaining) for balanced systems
- Estimated player edge % from the current true count
- Bet recommendation ramp (1 → 2 → 4 → 8 → 12 units) tied to live count

### Counting Systems (7 total)
| System | Level | Type | Ace Side Count |
|--------|-------|------|----------------|
| Hi-Lo | 1 | Balanced | No |
| KO | 1 | Unbalanced | No |
| ReKO | 1 | Unbalanced | No |
| Hi-Opt II | 2 | Balanced | Recommended |
| Omega II | 2 | Balanced | Recommended |
| Zen Count | 2 | Balanced | No |
| Wong Halves | 3 | Balanced | No |

All systems are unlocked by default — no in-app purchases.

### Table Rules Configuration
Match the app to real table conditions before you sit down:
- Dealer Soft 17: S17 or H17
- Dealer Peek: Ace+10 / Ace only / No peek (European)
- Blackjack Payout: 3:2 or 6:5
- Double Down: Any 2 cards / 9–11 / 10–11
- Double After Split (DAS)
- Surrender: None / Late / Early
- Resplit Aces
- Hit Split Aces
- 6-Card Charlie Auto Win
- Max Split Hands (2–6)

### Shoe & Deck Tracking
- Set total decks (1 / 2 / 4 / 6 / 8) with a tap
- Adjust decks remaining with a precision slider (0.25 deck steps)
- Live penetration %, cards drawn, cards left, total cards
- Optional Ace side count (for Hi-Opt II and Omega II)

### Strategy Reference
- Full-screen basic strategy chart — rule-aware, updates with current true count
- Strategy deviations marked with * on chart
- Illustrious 18 + Fab 4 indices, scaled to each system's own true count
- System cheat sheet (tag values + key deviations) per system
- Counting system guides with level, balance type, and usage tips

### Session Tools
- Recent cards history (last 12–15 cards visible)
- One-tap Undo for correcting a missed or wrong card tap
- Reset returns shoe and count to starting state, with a confirmation so a stray tap can't wipe a shoe
  - KO / ReKO reset returns to the deck-specific Initial Running Count (IRC), not zero
- Session resume — running count, shoe depth, selected system, and table rules survive app restarts (stored locally)
- Built for live use — haptic tick on every card tap, screen stays awake while counting, system back closes the strategy chart

### Drill Mode
- Timed running-count practice from the Menu
- Pick length (26 / 52 / 78 cards) and speed (Slow / Medium / Fast)
- Cards are dealt from a shuffled real shoe and flash one at a time; enter the final running count when done
- Scored exact / off-by-N against the active counting system

---

## 5. Target Audience

**Primary:** Aspiring and active card counters who train seriously between sessions.
Practice the full workflow — count, adjust bet, check edge — without relying on a training partner.

**Secondary:** Casino strategy enthusiasts who want to understand counting system mechanics
side-by-side, or verify their table rule configuration affects strategy actions.

**Not for:** Casual blackjack players looking for a basic strategy app only.
(There are simpler options; this app rewards users who know what a true count is.)

---

## 6. Key Differentiators

Lead with these when contrasting against competitor apps:

1. **7 systems in one app** — most competitors offer 1–2
2. **Full table rules engine** — affects both strategy display and edge calculation
3. **Strategy deviations tied to live count** — not just a static chart
4. **No subscriptions, no ads, no locked systems** — all features available on install
5. **Compact one-screen workflow** — count + bet + shoe controls all visible without scrolling
6. **KO / ReKO with proper IRC** — unbalanced systems reset to their deck-specific starting count, not 0

---

## 7. Visual Language

### Color Palette (Brand Tokens)
| Name | Hex | Use |
|------|-----|-----|
| Felt Green | `#0E3A2F` | Background, primary surface |
| Felt Green Bright | `#1C6D57` | Secondary surface, accents |
| Table Edge | `#091B17` | Deepest background, edges |
| Gold Accent | `#D8B04C` | Primary action color |
| Gold Bright | `#F1C96A` | Header text, active labels |
| Chip Red | `#B83A3A` | Negative count, warnings |
| Chip Green | `#2D8A45` | Positive count, advantage state |
| Text Light | `#E8F2ED` | Body text on dark |
| Warm Text | `#F5E9CB` | Secondary text on dark |
| Muted Text | `#B7C8C1` | Labels, metadata |

### Design Tone
- Dark, high-contrast — suited for use in dim casino lighting
- Angular shapes (4–6dp corners) — not bubbly Material default
- Dense but legible — information-packed without clutter
- Casino-adjacent without being kitschy — no playing card clip art, no green felt textures in UI

### Screenshot Opportunities
- Hero: Running count card in large hero display showing "+8" or "-4"
- Feature: Card input pad with color-coded buttons (red / neutral / green)
- Feature: Strategy chart full-screen with deviation markers
- Feature: System selector showing all 7 systems
- Feature: Table rules dialog
- Feature: Bet ramp with active dots lit

---

## 8. How It Works — 5-Step Summary

For "How to use" sections or explainer blocks on the website:

1. **Set the table** — Open Menu → Table Rules and configure the conditions at your table.
2. **Pick a system** — Choose from Hi-Lo (beginner) through Wong Halves (advanced).
3. **Set the shoe** — Choose deck count and slide decks remaining as play progresses.
4. **Tap as cards land** — Running count, true count, edge %, and bet units update instantly.
5. **Check the chart** — Hit Strategy Table for live deviation-aware basic strategy.

---

## 9. Bet Ramp Explained (for FAQ / feature detail)

The app recommends bet units from the ramp **1 → 2 → 4 → 8 → 12**.
A "unit" is your own base bet amount — the app teaches you *when* to size up, not how much to bet.

**Balanced systems (Hi-Lo, Hi-Opt II, Omega II, Zen Count, Wong Halves):**
- True count < 1: 1 unit
- True count 1–2: 2 units
- True count 2–3: 4 units
- True count 3–4: 8 units
- True count ≥ 4: 12 units

**Unbalanced systems (KO, ReKO):**
- Below Key Count: 1 unit
- At Key Count: 2 units
- At Pivot: 4 units, then 8, then 12 as count climbs

---

## 10. Copy Blocks

### Hero Section
**Headline:** Count every card. Win every edge.
**Subhead:** The only card-counting trainer with 7 professional systems, a live true-count engine, and full table-rules support — all free, all on one screen.
**Primary CTA:** Download APK — Android → `https://github.com/vectorlogic-dev/card-counter/releases/latest`
**Secondary CTA:** View on GitHub → `https://github.com/vectorlogic-dev/card-counter`

### Features Section Header
**Headline:** Everything a serious counter needs
**Subhead:** No subscriptions. No ads. No watered-down free tier. Just the tool.

### Counting Systems Section
**Headline:** Seven systems. One app.
**Subhead:** From Hi-Lo beginners to Wong Halves experts — switch systems anytime and see how the bet ramp and strategy deviations shift.

### Table Rules Section
**Headline:** Your table. Your rules.
**Subhead:** Configure 10 rule parameters before you sit down. Edge calculations and strategy deviations update automatically.

### Footer Disclaimer
*Counting Edge is a training tool for educational purposes.
It is not gambling advice, and it does not guarantee casino winnings.
Play responsibly.*

---

## 11. SEO Keywords

**Primary:**
- card counting app android
- blackjack card counter android
- hi lo counting app
- blackjack trainer android

**Secondary:**
- true count calculator
- blackjack counting systems
- KO counting system app
- wong halves card counting
- blackjack edge calculator
- blackjack table rules trainer

---

## 12. FAQ Copy

**Q: Is Counting Edge free?**
A: Yes. All 7 counting systems, the full strategy chart, and every feature are available on install — no in-app purchases, no subscription, no ads.

**Q: Which counting system should I start with?**
A: Hi-Lo is the standard starting point. It's balanced, level 1, and the most widely documented system. You can switch to KO or ReKO if you want to skip true count conversion. Advanced systems like Hi-Opt II and Wong Halves are there when you're ready.

**Q: What does "true count" mean?**
A: The true count is the running count divided by the estimated number of decks remaining. It normalizes the count across different shoe sizes, making your edge estimate more accurate. Unbalanced systems like KO don't require true count conversion by design.

**Q: Is this legal to use in a casino?**
A: Card counting by itself is not illegal. Using software or devices at the table violates casino rules in most jurisdictions. Counting Edge is a training tool — use it to practice, not at the table.

**Q: What are the system levels?**
A: Level 1 systems (Hi-Lo, KO, ReKO) use simple ±1 tag values and are the easiest to learn. Level 2 systems (Hi-Opt II, Omega II, Zen Count) use a wider tag range for a small accuracy gain. Level 3 (Wong Halves) uses fractional tags for maximum precision, requiring more mental effort.

---

## 13. Privacy & Compliance Notes

- No analytics, crash reporting, or tracking SDKs
- No account required, no sign-in, no network calls
- Privacy policy: `docs/privacy-policy.md` in repo / hosted at developer URL
- Suitable for all ages (no gambling with real money in-app)

---

## 14. Distribution & Download

| Field | Value |
|-------|-------|
| Channel | Direct APK |
| Primary host | `https://blackjackcounter.app` (download CTA points at the latest GitHub Release asset) |
| Mirror | `https://github.com/vectorlogic-dev/card-counter/releases/latest` |
| Latest version | `v1.0.0` |
| APK filename | `CountingEdge-<version>.apk` |
| APK integrity | Each release notes file publishes the SHA-256; the site should surface it next to the download button |
| Signing | PKCS12 keystore, self-signed. Signing certificate SHA-256: `69:B1:04:CB:37:BC:E3:1E:B6:11:71:08:D3:7E:A4:2F:57:3F:1C:9B:44:45:3A:23:2E:25:9D:69:91:83:39:8E` |
| License | Apache-2.0 |
| Min Android | 7.0 (API 24) |
| Content rating | Everyone |

**Install guidance (place near the download button):**

> Download the APK, then open it on your Android device. The first time you install from your browser (or file manager), Android asks whether to allow installs from this source — tap allow for that one app. Updates install over the top; app data is preserved.

### Fetching the latest release

The site should pull the latest release metadata from the GitHub API at build time (or client-side) so the download button always points at the newest APK without a manual bump:

```
GET https://api.github.com/repos/vectorlogic-dev/card-counter/releases/latest
```

Use `assets[].name == "CountingEdge-<version>.apk"` for the direct download URL, `tag_name` for the version label, and the release body for the changelog excerpt. Fall back to the raw `/releases/latest` HTML URL if the API call fails or is rate-limited.
