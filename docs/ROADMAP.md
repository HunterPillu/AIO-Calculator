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
| 6+ | SIP, lumpsum, SWP, savings, tax, converter, utility, history, and release features | NOT STARTED |

The project is intentionally built incrementally. A phase is marked complete only after implementation, validation, tests, build verification, regression review, and documentation are addressed.

Phase 2 adds scientific functions to the original parser rather than introducing a second expression engine. It supports degree/radian trigonometry, logarithms, square root, powers, factorial, constants, reciprocal, and square operations.

Phase 3 adds a searchable calculator catalog and a shared home screen. Only calculators with real implementations are listed; future calculators are not represented as placeholder screens.

Phase 4 adds validated EMI calculations for monthly or yearly tenure input, total principal/interest/payment breakdowns, and a first-year amortization preview backed by a complete schedule in the result model.

Phase 5 compares reducing tenure versus reducing EMI and supports monthly, quarterly, half-yearly, and yearly periodic prepayments. Loan plans handle zero interest, final-payment residuals, and invalid repayment assumptions explicitly.
