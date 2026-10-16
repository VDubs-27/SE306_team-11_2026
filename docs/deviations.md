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

## 6. Co-borrower type is required

- **Proposal:** Table 1's co-borrower rules cover which customer can be added, but not the co-borrower type.
- **Code:** `Coborrower.Details` requires a type of co-borrower or guarantor.
- **Justification:** the mainframe's add co-borrower message sends a type, and the guarantor type is needed for the Table 1 rule that a customer with no employer needs a guarantor.
- **Introduced in:** PR #5.

## 7. Loan totals and repayment schedule have their own types

- **Proposal:** Section 3.1 says summaries and schedules are not modelled as entities.
- **Code:** `Loan.Totals` holds the result of the loan summary message, and `ScheduledRepayment` holds one row of the repayment schedule.
- **Justification:** they are read-only display types for the required loan summary and repayment schedule screens, not entities.
  They have no identity, validation or create, update and delete operations, and exist so the UI receives typed values instead of raw mainframe strings.
- **Introduced in:** PR #5.

## 8. Phone number list uses LAVS's own message class

- **Proposal:** Section 4.2 says the mainframe adapter translates domain objects to and from the provided message classes.
- **Code:** `PhoneGateway` lists phone numbers with `PhoneNumberList`, a LAVS message class in `lavs.logic.adapter`, instead of the provided `customerPhoneNumber.List`.
- **Justification:** the provided class has a bug: its `Item` constructor writes the phone number to a field of the outer `List`, so every item reports the last item's number.
  Tested against the live mainframe, two phones numbered `9 555 0001` and `9 555 0002` were both listed as `9 555 0002`.
  `PhoneNumberList` sends the same `customerPhoneNumber.list` request and reads each item's own number.
  The provided code is left unedited, as the readme requires.
- **Introduced in:** PR #19.

## 9. DIT excludes JDK library classes

- **Proposal:** Section 6 sets DIT ≤ 3 "within the team's own classes, excluding JavaFX library classes".
- **Code:** the CI metrics step works out DIT from source, counting only classes declared under `src/main/java/lavs`. The depth stops at the first parent that isn't a team class, so JDK ancestors are excluded as well as JavaFX ones. CK's own DIT is still shown alongside in the CI summary.
- **Justification:** CK counts every ancestor, so any custom exception extending `RuntimeException` scores DIT 4 from JDK depth the team can't change (e.g. `MainframeException`). Excluding the JDK follows the same reasoning as excluding JavaFX: the target measures hierarchies the team designs, not library ones.
- **Introduced in:** PR #11.

## 10. Mainframe adapter is split into a shared adapter and per-area gateways

- **Proposal:** Sections 2.2 and 4.2 describe one mainframe adapter that translates domain objects to and from the provided message classes and turns status codes into meaningful errors.
- **Code:** `MainframeAdapter` only runs a provided message (`send`) and turns failures into `MainframeException`. One gateway per area (`CustomerGateway`, with loan and co-borrower to follow at time of writing) builds the provided message, calls `adapter.send`, and maps the getters into domain records so that services depend on the gateways, not the adapter.
- **Justification:** the provided message classes already build their own request and unpack their own response, so one adapter building requests would duplicate them. `Message.send` throws a checked `IOException` and catching it once in `send` and rethrowing it as `MainframeException` stops it spreading to the services and UI. Splitting by area also keeps each class small and lets the work be reviewed in parts. UI still never touches the mainframe.
- **Introduced in:** PR #11 (adapter foundation), PR #26 (customer gateway).

## 11. Loan document fields are validated

- **Proposal:** Table 1 has no rules for loan documents, and Figure 2 lists documents as an optional entity without fields.
- **Code:** `LoanDocument.Details` requires a document type, file name and file type, and rejects a negative file size.
- **Justification:** the mainframe's add document message sends all four fields, and a document without a type or file name cannot be identified in the documents list.
  The fields follow the `loan.addDocument` message, as Section 3.1 says field names follow the mainframe interface messages.
- **Introduced in:** PR #23.

## 12. Loan document update adds a replacement and deletes the original

- **Proposal:** Section 3.1 says each domain object maps directly to a mainframe message.
- **Code:** `LoanDocumentService.update` checks the original still exists, adds the new details as a new document, then deletes the original. It returns the new document, which has a new id.
- **Justification:** the brief requires adding, updating and deleting loan documents, but the mainframe has no update message for them.
  Tested against the live mainframe, `loan.updateDocument` and `loanDocument.update` are both rejected as unknown transactions.
  This follows the proposal's approach for co-borrowers in Table 1, where changes are made by deleting and re-adding.
  Adding before deleting means a failure can leave a duplicate, which the user is told to delete, but never loses a document.
- **Introduced in:** PR #23.
