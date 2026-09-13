# Architecture

The application uses Kotlin Multiplatform with shared Compose UI and shared calculation logic:

```text
Compose UI
    |
Feature state and event handling
    |
Pure calculator engine
    |
Typed calculation result
```

The `shared` module owns code that can run on Android and iOS. Platform entry points remain thin: Android starts `App()` from `MainActivity`, while iOS embeds the shared `MainViewController`. `App()` presents the catalog first and routes to implemented calculator screens without introducing a navigation dependency for this small feature set.

Calculator engines live below `app.prinkal.calculator.feature`. They do not depend on Compose, platform APIs, or formatted UI strings. The simple and scientific calculators share one hand-written tokenizer and recursive-descent parser; it never evaluates arbitrary code. Scientific angle mode is an explicit input to the shared evaluator, and display formatting remains a UI concern.

`feature/catalog/CalculatorCatalog` is the single source of truth for implemented calculator metadata. Adding a new calculator requires adding its definition and wiring its screen at the home boundary, rather than scattering labels across the UI.

Financial engines return typed success/error results. The EMI engine calculates without display rounding, produces a complete amortization schedule, and keeps the Compose screen responsible only for input conversion and presentation.

Loan prepayment calculations reuse a small repayment-plan simulation for both one-time and periodic prepayments. The comparison result keeps tenure, EMI, and interest-saved values typed so screens do not contain financial formulas.
