# Contributing

Thanks for your interest in Counting Edge: Card Counter. Contributions of all sizes are welcome — bug reports, feature ideas, documentation fixes, and code.

## Reporting issues

Open a GitHub issue with:
- What you expected to happen
- What actually happened
- Steps to reproduce (device model, Android version, counting system in use)

## Development setup

Requirements:
- Android Studio (latest stable) or JDK 17 + Android SDK on the command line
- Android SDK platform 36 (compile), minimum 24

```bash
git clone https://github.com/vectorlogic-dev/card-counter.git
cd card-counter
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

The debug APK lands at `app/build/outputs/apk/debug/app-debug.apk`.

## Submitting changes

1. Fork and create a feature branch off `main`.
2. Keep commits focused — one logical change per commit is easier to review.
3. Run `./gradlew testDebugUnitTest lintDebug` before pushing.
4. Open a pull request describing what changed and why.

CI runs tests and builds a debug APK on every pull request; please check that it passes.

## Style

- Kotlin + Jetpack Compose, MVVM
- Match existing patterns in the module you're editing
- Prefer small, pure functions in `domain/` and keep Compose code declarative

## License

By contributing, you agree that your contributions will be licensed under the Apache License 2.0 (see [LICENSE](LICENSE)).
