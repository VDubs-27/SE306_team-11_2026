# Deviations from the proposal

The proposal is locked, so the code is expected to match it.
This log records every deliberate difference between the code and the proposal, and why it was made.
Add an entry here whenever a change knowingly departs from the proposal.

## 1. Family name is required

- **Proposal:** Table 1 requires only `firstName` for a customer.
- **Code:** `Customer.Details` requires both `firstName` and `familyName`.
- **Justification:** Table 1 followed the mainframe screenshots on Canvas, which mark only `firstName` as required.
  The lecturer later confirmed on Ed Discussion that both `firstName` and `familyName` are required.
- **Source:** lecturer's Ed Discussion post (link to be added).
- **Introduced in:** PR #4.

## 2. Post code is stored as text

- **Proposal:** Figure 2 types `postCode` as an integer.
- **Code:** `Address.postCode` is a `String`, validated as digits only when present.
- **Justification:** an integer would drop leading zeros, so a post code such as `0600` would be saved as `600`.
  The digits-only check still enforces the Table 1 rule that `postCode` is numeric.
- **Introduced in:** PR #4.

## 3. Salary is stored as a decimal

- **Proposal:** Figure 2 types the employer salary as a string.
- **Code:** `Employer.salary` is a `BigDecimal`.
- **Justification:** a string cannot be validated or used in calculations, and a floating-point type introduces rounding errors in money.
- **Introduced in:** PR #4.

## 4. Other type needs a description

- **Proposal:** Table 1 has no rule for the `otherType` field.
- **Code:** address, phone and email `Details` require `otherType` when the type is `OTHER`.
- **Justification:** without it the mainframe would store a type of "Other" with no description of what it is.
- **Introduced in:** PR #4.

## 5. Email type is required

- **Proposal:** Table 1 only sets rules for the email address itself.
- **Code:** `Email.Details` also requires a type.
- **Justification:** the mainframe's add and update email messages send a type, and without one an email cannot be categorised.
- **Introduced in:** PR #4.
