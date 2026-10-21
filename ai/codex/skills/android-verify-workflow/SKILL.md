---
name: android-verify-workflow
description: Run a reliable local verification workflow for this Android repository. Use after Kotlin or Compose edits, before handoff, or when diagnosing Gradle failures. Handle JAVA_HOME setup, run build and test tasks, and report concise actionable failures.
---

# Android Verify Workflow

## Overview

Run fast, repeatable verification for this repo with predictable JDK setup.
Prefer the provided script so checks are consistent across sessions.

## Quick Start

Run from repository root:

```powershell
powershell -ExecutionPolicy Bypass -File ai/codex/skills/android-verify-workflow/scripts/verify.ps1
```

Optional full run:

```powershell
powershell -ExecutionPolicy Bypass -File ai/codex/skills/android-verify-workflow/scripts/verify.ps1 -IncludeUnitTests -IncludeLint
```

## Workflow

1. Ensure JDK availability.
2. Prefer Android Studio bundled JBR if present:
   - `C:\Program Files\Android\Android Studio\jbr`
3. Run Gradle tasks in this order:
   - `assembleDebug`
   - `testDebugUnitTest` (optional)
   - `lintDebug` (optional)
4. If a task fails, return:
   - the failed task
   - first actionable compiler/error lines
   - likely fix location (file + symbol)

## Fallback Commands

If the script cannot be used:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat assembleDebug
```

## Resource

Read `references/failure-triage.md` when you need concise error diagnosis patterns.
