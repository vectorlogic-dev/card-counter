# Redesign Rules

## Visual Hierarchy

- Choose one dominant metric and one supporting metric tier. In this app, `Running` and `True` count should usually outrank everything else.
- Avoid three equally loud cards in a row unless they truly carry equal importance.
- Use stronger type scale, accent, or surface treatment for the metric that should drive the next user action.

## Surface System

- Use a small set of recurring surface roles:
  - hero surface for live count state
  - utility surface for shoe/deck controls
  - action surface for card entry
  - quiet utility surface for undo/history/help
- Differentiate these roles with elevation, outline, tint, or accent strip choices.
- Avoid stacking many identical `Card` components with only text changes.

## Density

- Compress labels before shrinking tap targets.
- Remove decorative duplication before reducing core content size.
- Keep fast-tap controls large enough to hit repeatedly under pressure.

## Typography

- Use typography to create confidence and rhythm:
  - large, stable numerals for count values
  - compact labels with lower emphasis
  - restrained uppercase usage for section tags only
- Make numeric information easier to scan than surrounding prose.

## Color

- Keep the existing blackjack/table identity, but sharpen contrast and intent.
- Reserve bright accent color for important values and active states.
- Avoid coloring every component strongly; most surfaces should stay calm so the important values pop.

## Interaction

- Put the highest-frequency actions closest to the natural thumb zone.
- Keep undo visibly available because the workflow is error-prone by nature.
- Make rarely used reference content feel secondary and dismissible.

## Motion

- Prefer a few meaningful transitions:
  - count value change emphasis
  - dialog/sheet entrance
  - subtle reveal of secondary info
- Do not add decorative motion to high-frequency controls.

## Dialogs And Menus

- If a modal contains dense information, make it feel intentional:
  - stronger title hierarchy
  - clearer section grouping
  - comfortable internal spacing
- Avoid default-looking alert dialogs for content that acts like a real screen.

## Repo-Specific Suggestions

- Reconsider the equal-weight stat trio in `CounterScreenContent.kt`; a hero metric block plus smaller support metrics may read better.
- Treat `CountButtonsRow.kt` as a core interaction zone, not just another section card.
- `BetRecommendationCard.kt` should feel more like a live recommendation and less like a generic info row.
- Use `Color.kt` and `Type.kt` to define the redesign language first, then restyle components against that system.
