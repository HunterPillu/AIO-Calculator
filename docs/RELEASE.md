# Version 1.0 Release Plan

## Release scope

Version 1.0 contains the implemented calculators:

- Simple calculator
- Scientific calculator
- EMI
- Loan prepayment
- SIP
- Lumpsum

SWP, SIP + Lumpsum, savings, tax, converters, utilities, history, favorites, and sharing are deferred to Version 1.1.

## Phases

| Phase | Scope | Status |
| --- | --- | --- |
| R0 | Scope freeze and version policy | COMPLETE |
| R1 | Diagnostics, settings, about, and disclaimers | COMPLETE |
| R2 | Android release hardening and signing configuration | IN PROGRESS |
| R3 | Android Play Store metadata, privacy, branding, and compliance | NOT STARTED |
| R4 | Android release candidate QA and artifact verification | NOT STARTED |
| R5 | Android internal/beta distribution | BLOCKED on Play Console and signing |
| R6 | Android production rollout and monitoring | BLOCKED on R5 |

## R0: Scope and version policy

- Android version name/code are supplied by `gradle.properties` (`app.versionName`, `app.versionCode`).
- Current target is `1.0.0 (1)`.
- Release tags should use `v1.0.0`.
- New calculator engines should target Version 1.1 unless they fix a release-blocking defect.

The common `AppInfo` values must stay aligned with Android build metadata until generated platform metadata is introduced.

## R1: Diagnostics and product information

- Flow diagnostics use Kermit through `core/logging/AppLogger`.
- Debug flow events contain only screen/calculator IDs and success/failure state.
- Calculation values, expressions, credentials, and personal data must not be logged.
- Android debug events are emitted while a debugger is attached; warnings remain available for release diagnostics.
- Settings exposes app version, developer information, privacy posture, open-source components, and financial disclaimer.

Crash reporting is not enabled yet. If it is added, obtain approval for the provider, retention policy, privacy disclosures, and mapping/dSYM upload process first.

## R2: Release hardening

### Android

- Release builds enable R8 and resource shrinking.
- Source locations are preserved for crash diagnosis; mappings must be stored privately.
- Android backups are disabled because Version 1.0 has no persisted user data.
- Release signing is parameterized and never stores secrets in Git.
- `verifyReleaseSigning` must pass before `assembleRelease` or `bundleRelease`.

Secure inputs:

```text
ANDROID_RELEASE_STORE_FILE
ANDROID_RELEASE_STORE_PASSWORD
ANDROID_RELEASE_KEY_ALIAS
ANDROID_RELEASE_KEY_PASSWORD
```

Equivalent `android.release.*` Gradle properties are also supported.

## Apple App Store track: deferred

Apple App Store publication is intentionally deferred until the Android Version 1.0 release is complete. The current iOS configuration is retained as a provisional foundation, but no App Store credentials, archive, or store submission work is part of the active release. This work is tracked by the `apple-app-store-release` task.

The tracked next-phase work includes:

- Confirm the final bundle identifier and Apple Team ID.
- Configure distribution certificates, provisioning, and App Store signing.
- Review iOS optimization, symbol stripping, and protected dSYM handling.
- Prepare App Store metadata, privacy declarations, screenshots, and support links.
- Archive and test the signed iOS release on macOS before submission.

## R3: Android Play Store and compliance inputs

The owner must provide or approve for the active Android release:

- Google Play Console access
- Final Android application ID
- Android upload keystore and Play App Signing setup
- Privacy policy URL and support/contact URL
- Google Play Data Safety declarations
- Financial disclaimer wording
- Android age rating, target countries, category, screenshots, icon artwork, description, and release notes

## R4: Android release candidate checks

- Run shared tests and Android release build.
- Install the signed artifact on representative Android devices.
- Verify launch, navigation, calculators, validation, settings, dark mode, font scaling, accessibility labels, rotation, and offline behavior.
- Confirm obfuscation, mapping files, version metadata, and signing identity.
- Check that no secrets, local paths, debug endpoints, or sensitive logs are present.

## Current blockers

Android publication is blocked until the owner confirms the application ID and supplies the upload keystore, Play Console access, and store metadata through secure channels. Do not paste keystore passwords, private keys, certificates, or store API keys into source files or chat.
