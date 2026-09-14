# Development

## Requirements

- Android Studio with Kotlin Multiplatform support
- Android SDK configured for the versions in `gradle/libs.versions.toml`
- Xcode for the iOS application

## Common commands

```text
gradlew.bat :shared:testAndroidHostTest
gradlew.bat :androidApp:assembleDebug
```

Open `iosApp` in Xcode to run the iOS target and its shared Compose UI.

## Conventions

- Keep calculation logic pure and independent from Compose.
- Validate input in the calculation layer and return explicit errors.
- Keep display rounding and formatting out of formulas.
- Prefer Kotlin and existing project dependencies before adding libraries.
- Add common tests for every calculator engine.
- Keep calculator input screens separate from result screens when results contain schedules or comparisons.
- Use shared `formatNumber`/`formatCurrency` for display values and do not round inside calculation engines.
- Use icons with content descriptions for navigation and calculator actions.
- Keep user-visible copy in `shared/src/commonMain/composeResources/values/strings.xml` and access it through Compose Resources.

## Release workflow

See [RELEASE.md](RELEASE.md) for the active Android Version 1.0 scope, hardening phases, signing inputs, Play Store requirements, and release-candidate checklist. Apple App Store work is tracked as a deferred next phase. Release secrets must be supplied through a secure secret store or local untracked configuration.

The iOS simulator test task is unavailable on Windows; run it from Xcode or macOS. Android host tests exercise the shared calculation logic on this platform.
