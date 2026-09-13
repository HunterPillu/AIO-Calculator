# Roadmap

## Status

| Phase | Scope | Status |
| --- | --- | --- |
| 0 | Foundation and project documentation | COMPLETE |
| 1 | Simple calculator and safe expression engine | COMPLETE |
| 2 | Scientific calculator functions | COMPLETE |
| 3 | Calculator home and catalog | COMPLETE |
| 4 | EMI calculator and amortization schedule | COMPLETE |
| 5 | Loan prepayment comparisons and periodic prepayment | COMPLETE |
| UI-1 | Modern visual foundation | COMPLETE |
| UI-2 | Dedicated financial result screens | COMPLETE |
| UI-3 | Icons, animations, and two-decimal formatting | COMPLETE |
| 6 | SIP calculator | COMPLETE |
| 7 | Lumpsum calculator | COMPLETE |
| 8+ | SIP + lumpsum, SWP, savings, tax, converter, utility, history, and release features | NOT STARTED |

The project is intentionally built incrementally. A phase is marked complete only after implementation, validation, tests, build verification, regression review, and documentation are addressed.

Phase 2 adds scientific functions to the original parser rather than introducing a second expression engine. It supports degree/radian trigonometry, logarithms, square root, powers, factorial, constants, reciprocal, and square operations.

Phase 3 adds a searchable calculator catalog and a shared home screen. Only calculators with real implementations are listed; future calculators are not represented as placeholder screens.

Phase 4 adds validated EMI calculations for monthly or yearly tenure input, total principal/interest/payment breakdowns, and a first-year amortization preview backed by a complete schedule in the result model.

Phase 5 compares reducing tenure versus reducing EMI and supports monthly, quarterly, half-yearly, and yearly periodic prepayments. Loan plans handle zero interest, final-payment residuals, and invalid repayment assumptions explicitly.

UI-1 introduces a shared light/dark theme, stronger visual hierarchy, rounded cards, calculator-specific iconography, category filters, and consistent spacing.

UI-2 moves EMI and loan-prepayment results to dedicated result routes with icon-based top-bar navigation, keeping input screens focused and preventing result content from extending the input form.

UI-3 adds animated route transitions, animated result entry, accessible icons, and shared display formatting capped at two fraction digits without changing calculation precision.

Phase 6 adds monthly SIP projections with optional annual step-up. Contributions are modeled at the beginning of each month, monthly compounding uses the expected annual return divided by 12, and the result includes yearly invested/return/value snapshots.

Phase 7 adds one-time lumpsum projections with annual compounding and yearly growth snapshots. Both investment calculators validate inputs in shared calculation engines and keep result presentation on dedicated screens.
