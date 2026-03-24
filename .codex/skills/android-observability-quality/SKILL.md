---
name: android-observability-quality
description: Raise this Android app to production quality with test coverage, regression gates, crash and ANR monitoring, analytics instrumentation, performance checks, and release-blocking validation. Use when adding tests, integrating monitoring, defining quality gates, or auditing whether the app is ready to survive real-world usage.
---

# Android Observability Quality

## Overview

Make failures visible before and after release.
Balance automation, monitoring, and lightweight instrumentation so the app remains maintainable.

## Workflow

1. Identify the highest-risk user flows first:
- app launch
- count updates and resets
- deck controls
- rule selection and strategy guidance
2. Add or tighten release gates:
- unit tests for domain and ViewModel rules
- UI tests only for flows that would be expensive to regress manually
- build, lint, and targeted regression verification
3. Add observability intentionally:
- crash reporting for production failures
- ANR/performance signals through Play Vitals or equivalent
- analytics only for decisions that will drive product changes
4. Review signal quality:
- meaningful event names
- low-noise error reporting
- no sensitive data in logs or telemetry
5. Finish with explicit gaps:
- what is covered
- what is still manual
- what should block release

## Repo Notes

- Favor tests around counting logic, deck math, rules, and recommendation thresholds before adding broad UI automation.
- Keep telemetry minimal for this app category; privacy and trust matter more than exhaustive analytics.
- Use `android-verify-workflow` after Kotlin, Compose, or Gradle changes.

## Guardrails

- Do not add analytics or crash tooling without naming the privacy and Play Console implications.
- Prefer a few stable regression checks over a large flaky UI suite.
- Keep event and metric definitions tied to concrete product questions.
