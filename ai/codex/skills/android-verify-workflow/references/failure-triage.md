## Failure Triage

When Gradle fails, report:

1. Failed task name (for example `:app:compileDebugKotlin`).
2. First actionable error line (compiler or manifest/resource error).
3. Most likely fix target:
   - file path
   - symbol/function/resource name
4. Next validation command after patch.

## Common Cases

- `JAVA_HOME is not set`
  - Set `JAVA_HOME` to Android Studio JBR and rerun.
- `compileDebugKotlin` errors
  - Fix source compile issue first before running broader checks.
- Resource merge or manifest errors
  - Fix XML/package/namespace mismatch, then rerun `assembleDebug`.
- Lint-only failures
  - Separate functional fixes from style/lint suppression decisions.
