# Calculator Catalog

## Implemented

### Simple calculator

Supports decimal arithmetic, negative values, parentheses, operator precedence, postfix percentages, scientific notation, clear, backspace, sign toggle, and divide-by-zero validation.

The expression engine calculates with `Double` precision and leaves display formatting to the UI. Percentages are interpreted as a postfix value divided by 100, so `200 * 10%` evaluates to `20`.

### Scientific calculator

Uses the same expression engine as the simple calculator and adds:

- `sin`, `cos`, `tan`, `asin`, `acos`, and `atan` in degree or radian mode
- `log`, `ln`, `sqrt`, `square`, and `reciprocal`
- exponentiation (`^`), factorial (`!`), `π`, and `e`

Functions use parentheses, for example `sin(30)` or `sqrt(81)`. Domain errors and non-finite results are returned as explicit evaluation errors.

### EMI calculator

Inputs are loan amount, annual interest rate, and tenure in months or years. Results include monthly EMI, total principal, total interest, total payment, and an amortization schedule. Zero-interest loans are handled without applying the standard-interest formula. Display values are not rounded inside the calculation engine.

### Loan prepayment

Inputs are outstanding principal, annual interest rate, current EMI, remaining tenure, and a prepayment amount. The calculator compares reducing tenure while keeping EMI unchanged with reducing EMI while keeping tenure unchanged. It also supports periodic additional payments monthly, quarterly, half-yearly, or yearly and reports the interest saved.

EMI and loan-prepayment results open on separate result screens. Monetary and calculated result values are displayed with a maximum of two fraction digits; intermediate calculation values are not rounded.

## Planned next

Investment, savings, tax, converter, and general utility calculators will be added as independent features. Each will expose validated inputs, a typed result model, and unit tests for normal, boundary, invalid, and precision-sensitive cases.
