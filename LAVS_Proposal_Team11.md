# SOFTENG 306 — Team 11
## Loan Application and Verification System (LAVS) — Design Proposal

---

## 1 Introduction

Countrywide Banking Corporation uses a legacy mainframe system to run its backend services. It is fast and reliable, but accessed through a text-based terminal emulator designed in the 1980s. This emulator relies on shortcut keys, making it difficult to train new staff. Additionally, a single incorrect character forces the user to re-enter all information on a given screen, not just the incorrect field.

This proposal presents the design for the Loan Application and Verification System (LAVS), a JavaFX desktop application replacing the emulator as the mainframe's front-end. LAVS validates all data before it is sent, and guides Mobile Mortgage Managers (MMMs) through the bank's loan application process. This project will implement the required customer and loan functionality, plus four optional components: customer notes, customer documents, loan notes and loan documents.

## 2 System Model

The LAVS consists of two layers. These are the user interface layer which users will interact with and the business logic layer that will respond to such user interactions. External to this is the mainframe which possesses customer and loan data that can be processed with the business logic layer. The user interface layer never interacts directly with the mainframe as the business logic layer acts as this intermediary instead. This visualisation and the system's relationship to the connection interface can be seen below in Figure 1.

> **Figure 1:** LAVS system model showing the two layers, mainframe and Connection interface.
>
> - **Mobile Mortgage Manager** — a loan expert interacting with the software ↔ **User Interface**
> - **User Interface** (front layer presented to user handling direct interactions with the product): Login, Customer Search, New Customer, Loan Details, Customer Profile, New Loan
> - **Business Logic** (application rules, handles the program's main functionality, and data management): Validation, Loan Service, Domain Objects, Constants, Customer Service, Mainframe Adapter
> - Business Logic ↔ **Connection interface** (send/receive messages) ↔ **Mainframe** (system of record)
> - Connection interface implementations: **HttpConnection** (implements connections interface), **StubConnection** (to use in unit tests)

### 2.1 Layers and their components

Beginning with the **user interface (UI)** layer, its main responsibility is to render screens, capture user inputs, show any errors, and general user progress when using the UI. There are 6 associated views in this layer. These are login for authentication, customer search for finding customers, customer profile for a customer's details, loan details for each loan applied for, and wizards for new loans and customers. Each of these views depends on services from the business logic layer to process and respond to the interactions of users on these views.

Secondly, the **business logic layer** is the intermediary between mainframe and UI layer which has the responsibility of handling the program's main functionality, application rules, and data management. This layer handles the customer service and loan service which aid in customer and loan management respectively (e.g. notes and documents) where both components rely on validation before sending data, the mainframe adapter for message translation, and domain objects for in-memory storage. Validation enforces the rules in section 3.2 before any messages are sent.

### 2.2 Example Application Walkthrough

The scenario begins when a mobile mortgage manager saves a new loan. The new loan wizard controller would pass the form values inputted on the UI to the loan service in the business logic layer. This loan service then runs validation against validation rules and constants. If a check fails, the invalid fields are highlighted and nothing is sent to the mainframe. If successful, the loan service passes the loan and selected customer's number to the mainframe adapter which builds a `loan.create` message and sends it via Connection. The returned loan number is stored in the loan domain object and can be shown in the UI under loan details or elsewhere.

## 3 Data Schema

LAVS is a front end display, not the system of record. All customer and loan data is stored and processed by the legacy mainframe system, and can only be accessed through the provided interface messages like `customer.create` or `loan.retrieve`. LAVS therefore has no requirement for its own database. It has also been confirmed that the data does not need to be stored locally, and that a persistent stable connection between MMMs and the mainframe can be assumed.

The decision to not maintain a database within LAVS brings three benefits. There is one source of truth, so LAVS can never display data that disagrees with the mainframe. Secondly, less customer personal information is kept on MMM laptops, which could be lost or stolen. Finally, there is less code to build and test, which keeps the business logic layer small enough to attain the required 95% unit test code coverage. The only data the LAVS will hold will be data used in the current session: the records being viewed or edited as domain objects in the business logic layer and the constant category values which are loaded once at login and used for drop down menus and validation.

### 3.1 Domain Model

Figure 2 shows the entities LAVS works with. Field names follow the mainframe interface messages so each domain object maps directly to a message. This keeps the mainframe adapter (which translates between domain objects and the provided message classes) and the provided message classes simple, and straightforward to unit test against a mock connection. Figure 2 also includes the Loan Payment and Customer Relationship entities for completeness, although they will not be implemented. Our four optional components are customer notes, customer documents, loan notes and loan documents. Loan summaries and repayment schedules are calculated by the mainframe and only displayed, so they are not modelled as entities.

> **Figure 2:** Domain model. Keys link to Customer (customerNumber) or Loan (loanNumber). Each parent has a 1:N relationship with its children.

**CUSTOMER**

| Field | Type |
|---|---|
| customerNumber (PK) | string |
| title | string |
| firstName | string |
| familyName | string |
| status | enum |
| dateOfBirth | date |
| nationality | string |

**ADDRESS**

| Field | Type |
|---|---|
| id (PK) | long |
| customerNumber (FK) | string |
| type | enum |
| otherType | string |
| line1 | string |
| line2 | string |
| suburb | string |
| city | string |
| postCode | integer |
| country | string |
| isPrimary | boolean |
| isMailing | boolean |

**EMPLOYER**

| Field | Type |
|---|---|
| id (PK) | long |
| customerNumber (FK) | string |
| type | enum |
| name | string |
| position | string |
| salary | string |

**PHONE**

| Field | Type |
|---|---|
| id (PK) | long |
| customerNumber (FK) | string |
| type | enum |
| otherType | string |
| countryCode | string |
| number | string |
| isPrimary | boolean |
| canTxt | boolean |

**EMAIL**

| Field | Type |
|---|---|
| id (PK) | long |
| customerNumber (FK) | string |
| type | enum |
| otherType | string |
| address | email |
| isPrimary | boolean |

**CUSTOMER DOCUMENT**

| Field | Type |
|---|---|
| id (PK) | long |
| customerNumber (FK) | string |
| type | enum |
| fileName | string |
| fileType | string |
| size | string |

**CUSTOMER NOTE**

| Field | Type |
|---|---|
| id (PK) | long |
| customerNumber (FK) | string |
| type | enum |
| lines | string |
| author | string |
| dateAdded | date |

**RELATIONSHIP** *(not implemented)*

| Field | Type |
|---|---|
| id (PK) | long |
| customerNumber (FK) | string |
| type | enum |
| otherType | string |
| secondaryCustomerNumber (FK) | string |

**LOAN**

| Field | Type |
|---|---|
| loanNumber (PK) | string |
| customerNumber (FK) | string |
| principalAmount | double |
| rateType | string |
| rateAmount | double |
| rateFrequency | string |
| status | enum |
| startDate | date |
| paymentAmount | double |
| paymentFrequency | string |
| renewalDate | date |

**LOAN NOTE**

| Field | Type |
|---|---|
| id (PK) | long |
| loanNumber (FK) | string |
| type | enum |
| lines | string |
| author | string |
| dateAdded | date |

**LOAN DOCUMENT**

| Field | Type |
|---|---|
| id (PK) | long |
| loanNumber (FK) | string |
| type | enum |
| fileName | string |
| fileType | string |
| size | string |

**COBORROWER**

| Field | Type |
|---|---|
| id (PK) | long |
| loanNumber (FK) | string |
| type | enum |
| coborrowerNumber (FK) | string |

**PAYMENT** *(not implemented)*

| Field | Type |
|---|---|
| id (PK) | long |
| loanNumber (FK) | string |
| date | date |
| amountPaid | double |
| principalAmount | double |
| interestAmount | double |
| remainingAmount | double |

Customers and loans are identified by text numbers that the mainframe generates on creation (customerNumber, up to 15 characters; and loanNumber, up to 19 characters). Every child record, such as an address or a co-borrower, is identified by a numeric id that the mainframe returns when the record is added. Each child also holds its parent's id number as a foreign key. The interface messages are not consistent in naming these keys (for example, the loan number is called `number` in retrieve messages and `loanNumber` elsewhere), so Figure 2 uses one name throughout and the adapter (gateway) maps between them.

Customer is the central entity. A customer can have zero or more addresses, phone numbers, email addresses, and employers. Each carries a primary boolean flag (addresses also carry a mailing boolean flag), so a customer can hold many contact points while making one of each type preferred.

Each loan has one primary borrower. Co-borrowers and guarantors form a many-to-many relationship between Customer and Loan, resolved by the Loan Coborrower associative entity, which has its own id and a type of either Coborrower or Guarantor.

### 3.2 Validation Rules

Table 1 lists the rules the business logic layer enforces before any message is sent. Field lengths and required flags come from the mainframe screen definitions; the remaining rules come from the loan application process in the brief. Checking these locally gives the MMM immediate, field-level feedback instead of generic rejection from the mainframe.

| Entity | Rules |
|---|---|
| Customer | firstName is required; title ≤ 10 chars; names and nationality ≤ 40 chars; dateOfBirth must be a valid past date. All dates are sent in yyyy-MM-dd format. |
| Address | type, line1, and country are required; text fields ≤ 60 chars; postCode is numeric; at most one primary address; at least one mailing address is needed before a loan can proceed. |
| Phone / Email | The customer needs at least one phone number or email address; a phone needs a type, country code, and number; an email address must be validly formatted and ≤ 60 chars; at most one primary of each. |
| Employer | type and name are required. A customer with no employer needs a guarantor on the loan. |
| Loan | All nine input fields are required; principal > 0 and rate ≥ 0; enumerated values must match the constants loaded at login; renewalDate must be after startDate. |
| Loan Status | An MMM may only move a loan from New to Pending; other transitions are outside project scope. |
| Co-borrower | Must be an existing customer, and cannot be the primary borrower or a duplicate. Changes are made by deleting and re-adding, matching the available mainframe messages. |

*Table 1: Rules enforced by the business logic layer*

## 4 Data Flows

This section uses data flow diagrams to show how data moves between the MMM, LAVS, and the mainframe. External entities sit outside the system, processes transform data, data stores hold data at rest, and arrows are named data flows. The diagrams show what data moves where, not the order of screens, which is covered in Section 7 (GUI mockups) of the report.

### 4.1 Context Diagram (Level 0)

Figure 3 treats LAVS as a single process with two external entities: the MMM, who enters and reviews data, and the mainframe. The mainframe is shown as an external entity rather than a data store because LAVS cannot read or write its data directly; it can only exchange interface messages with it.

> **Figure 3:** Level 0 Context Diagram
>
> - **MMM → LAVS:** credentials, search terms, customer & contact details, loan & co-borrower details, status change
> - **LAVS → MMM:** validation feedback, confirmations & errors, customer & loan records, loan summary & repayment schedule
> - **LAVS → Mainframe (mock interface):** login, `customer.*`, `loan.*`, `constantItem.list` messages
> - **Mainframe → LAVS:** records, generated numbers/IDs, status & error codes, summary, schedule, constant values

### 4.2 Level 1 Data Flow Diagram

Figure 4 breaks LAVS down into seven processes. The only data store is D1, the constant values held in memory after login, because all other data stays on the mainframe. Two design decisions shape the diagram.

**Nothing is sent unvalidated.** Every change made in processes 3.0 and 4.0 passes through validation (5.0), which checks it against the rules in Table 1 (Section 3.2) and the constants in D1. Invalid data is returned to the MMM with the affected fields highlighted, and the values stay in the form so nothing has to be re-entered.

**One process writes to the mainframe.** Only process 6.0 sends create, update, and delete messages. Looking up data (processes 1.0, 2.0, and 7.0) uses retrieve and list messages. All messages go through the same mainframe adapter in the business logic layer, which translates domain objects to and from the provided message classes and turns status codes into meaningful errors. The adapter depends only on the provided Connection interface, not on HttpConnection, so unit tests can substitute the StubConnection supplied with the starter code and run without a live mainframe. The UI never talks to the mainframe directly. Table 2 traces the loan application process from the brief through the processes in Figure 4.

| Step (from the brief) | Processes and Data Flows |
|---|---|
| 1. Check whether the customer exists; add if not. | 2.0 searches the mainframe (`customer.search`). If there is no match, the MMM enters a new customer in 3.0. |
| 2–5. Verify details, mailing address, and alternate contact | 3.0 collects the changes; 5.0 checks the mailing address and phone-or-email rules; 6.0 sends `customer.create` or `customer.update`, then `customer.addAddress`, `customer.addEmail`, and so on. |
| 6. Update employment details | 3.0 (`customer.addEmployer`/`updateEmployer`). No employer triggers the guarantor rule. |
| 7–8. Start the loan and enter its details | 4.0 receives the customer number from 3.0; 5.0 validates the loan; 6.0 sends `loan.create`, which returns the new loan number. |
| 9. Show summary and repayments | 7.0 uses the loan number to retrieve the loan summary and repayment schedule from the mainframe. |
| 10. Add co-borrowers | Steps 1–6 are repeated for each co-borrower, then 6.0 sends `loan.addCoborrower` (type Coborrower or Guarantor). |
| 11. Change status from New to Pending | 4.0 and 6.0 send `loan.updateStatus` once all details are complete. |

*Table 2: Loan Application steps mapped to the Level 1 Data Flow Diagram (Figure 4)*

> **Figure 4:** Level 1 Data Flow Diagram
>
> Processes:
> - **1.0 Authenticate & Load Constants** — MMM sends credentials, receives login result; sends login, `constantItem.list` to Mainframe; receives session token, constant values; writes constants to D1.
> - **2.0 Find & Retrieve customer or loan** — MMM sends search term, receives matches; sends `customer.search`, `*.list`, `*.retrieve`; receives customer & loan records. Passes customer record to 3.0 and loan record to 4.0.
> - **3.0 Maintain customer & contact details** — MMM sends customer, contact & employer details; sends customer changes to 5.0; passes customer number to 4.0.
> - **4.0 Maintain loan & coborrowers** — MMM sends loan, co-borrower & status details; sends loan changes to 5.0; passes loan number to 7.0.
> - **5.0 Validate Changes** — reads allowed values from D1; returns validation errors to MMM; sends valid changes to 6.0.
> - **6.0 Submit to Mainframe** — sends create/update/delete; receives numbers, IDs, status codes; returns new loan number to 7.0 and confirmation/mainframe error to MMM.
> - **7.0 Present summary & schedule** — sends summary request; receives summary & schedule; returns summary & repayments to MMM.
>
> Data store: **D1 Constants**

### 4.3 Submitting Changes

Figure 5 expands processes 5.0 and 6.0 to show what happens when the MMM saves a change.

> **Figure 5:** Validating and submitting a change (detail of processes 5.0 and 6.0)
>
> 1. MMM presses save
> 2. 5.0: validate form against rules & cached constants
> 3. Valid?
>    - **No** → Highlight invalid fields; nothing is sent
>    - **Yes** → Disable Save; show progress indicator
> 4. 6.0 (background task): send messages in dependency order, e.g., `customer.create`, then `addAddress` with returned number
> 5. Status?
>    - **Success (0)** → Update view with returned numbers/IDs; confirm save
>    - **Server error (3)** → Show mainframe message; keep form values for correction
>    - **Network failure (10)** → Show connection error; keep form values so MMM can retry

**The interface must stay responsive.** Validation is fast and runs on the JavaFX application thread. Mainframe messages are sent in a background task (for example, a JavaFX Task) and have a timeout attached, while the UI disables the Save button and displays a progress indicator, so the application never hangs without informing the user.

**Messages are sent in dependency order.** Some changes need several messages, and later ones depend on values returned by earlier ones. For a new customer, *customer.create* must return the customer number before *customer.addAddress* or *customer.addEmail* can be sent. Process 6.0 sends these in sequence and stores each returned number or ID in the domain object.

**Responses are reported, not hidden.** Each message returns a Status with an error code. On success (0), the view is updated with the returned values and the save is confirmed. A server error (3) means the mainframe rejected the data, so its message is shown and the form keeps its values for correction. If part of a multi-message save fails, the MMM is told which parts were saved and which were not. We have been informed that we can assume the mainframe is available and does not need to handle it becoming unavailable.

## 5 Proposed Mainframe Integration

As set out in Sections 2.1 and 4.2, only the business logic layer communicates with the mainframe, which keeps business logic out of the UI and allows it to be tested independently.

Currently users need to be familiar with various keyboard shortcuts to be able to interact with the mainframe. We will be adding buttons in the graphical user interface (GUI) to replace the need for this, to improve ease of use and to reduce training time needed for new users to become proficient in processing loan applications. Currently users will input a full screen of data before submitting for validation. We will add checks for all the data fields to be carried out in the application, to prevent the need for the user to re-enter data should some or all of the data validation checks fail. Field checks will mirror the mainframe validation. Process rules will come from the brief.

A main pain point is that new MMMs must memorise keyboard shortcuts, and one incorrect character forces them to re-enter a whole screen of data. LAVS addresses this issue by displaying set lists of values as dropdown menus and adding date picker wheels to prevent incorrect data entry. Buttons will replace keyboard shortcuts to eliminate the need for keyboard shortcut use.

The application may run into a not found error when trying to update a record that hasn't been created, in which case the application will clearly display an error message and what the user should do to fix it. Concurrent edits to the same record are unlikely, because a customer's details are updated by the MMM handling their application, based on information from the customer. Therefore we will assume the last-write-wins rule will suffice. There should also rarely be a situation where the data is sent to the mainframe and rejected there, as the checks in the application mirror those on the mainframe. However, if this were to occur, we will display a clear error message explaining how to fix it, and keep the values the user entered in the form.

## 6 Proposed Software Metrics

Using internal attributes of the code, the team will assess external quality. Table 3 links each quality attribute to the metrics used to assess it.

| Quality attribute | Why it matters for LAVS | Metrics and targets | What a bad value indicates |
|---|---|---|---|
| Maintainability | The bank's loan rules and the mainframe's data fields will change over time, and each change means editing LAVS, so the code must be quick and safe to modify. | Cyclomatic complexity of 10 or less per method, a widely used limit. Conditional nesting depth of 3 or less. Weighted methods per class (WMC) compared across classes, with any class above twice the median reviewed. No single-letter identifiers except loop counters, following the course's Java coding standards and enforced by Checkstyle. Depth of inheritance tree (DIT) of 3 or less within the team's own classes, excluding JavaFX library classes. Number of children (NOC) of 10 or less. | Too many checks packed into one method, making it hard to read, change and test. It should be split into smaller methods, one per rule. A deep or wide hierarchy is hard to follow, and changing a parent class risks breaking its children. |
| Modularity | The user interface layer must contain no business logic. | Coupling between objects (CBO), kept low, with any class above twice the median for its layer reviewed. Fan-out of each UI controller, with any controller above twice the median controller fan-out reviewed. Fan-in of shared services, such as validation, is expected to be high. | A controller that uses many classes is probably doing business logic itself. Validation used by only a few classes suggests the same rules have been copied into many places. |
| Testability | The business logic layer needs at least 95% test coverage. | Line and branch coverage of at least 95% in the business logic layer. CBO as a predictor, since highly coupled classes are harder to test in isolation. | Untested validation paths, or classes that are hard to test with the mainframe mocks. |
| Reusability | Both wizards and the customer profile share form and list components. | Fan-in of each shared form and list component (e.g. address form, contact list) of at least 2, since each is used by a wizard and the customer profile. | A shared component with a fan-in of 1 means a screen has built its own copy of a form instead of reusing it. |
| Usability | MMMs must be able to use LAVS with minimal training. | Fog index of 8 or less, measured across all error and help messages combined, since single messages are too short for a reliable score. A score of 8 is plain English that can be understood on first reading. | Messages too hard to understand quickly while with customers. |

*Table 3: Quality attributes, the metrics that predict them, and their targets.*

The metrics will be used to find components that deviate from the rest of the codebase, not only to check thresholds. For example, a class with a much higher WMC or CBO than its peers will be reviewed and refactored, even if it is within our target.

The targets follow from the GUI design. Shared form components should show high fan-in, because both wizards and the profile use them. The wizard controllers are the most likely to grow complex, so their fan-out and cyclomatic complexity will be watched most closely.

These metrics predict quality, but do not prove it. Usability depends on how MMMs experience the application, so the Fog index is only one indicator used alongside feedback on the mock-ups.

## 7 Proposed GUI Mock-ups

LAVS replaces the 39 terminal screens in the provided mainframe screenshots with a login screen, customer search, a tabbed customer profile, a loan details screen, and two step-by-step creation wizards. The design targets two main problems:

- **Memorised shortcut keys make the terminal slow to learn.** LAVS uses labelled buttons, dropdowns, and wizards instead (recognition rather than recall).
- **One invalid character rejects the whole screen.** LAVS validates each field before it sends data to the mainframe. An invalid field is highlighted, and all other inputs are kept (error prevention).

> **Figure 6:** LAVS screen flow.
>
> - **Login** (username and password; entry point) → *Sign in* → **Customer Search** (find a customer by name or customer number; Brief Step 1)
> - Customer Search → *Add new* → **New Customer Wizard** (details, addresses, contacts, employment, review; Brief Steps 1–6) → *Save* → **Customer Profile** (tabs: Details, Addresses, Contacts, Employment, Loans, Documents, Notes; Brief Steps 2–7)
> - Customer Search → *Open existing customer* → Customer Profile
> - Customer Profile → *Open a loan* → **Loan Details** (view and edit a loan; co-borrowers, status, documents, notes; existing loans)
> - Customer Profile → *New loan* (needs a mailing address and a phone or email) → **New Loan Wizard**:
>   1. **Loan Details** — enter the loan; each field validated inline (Brief Step 8)
>   2. **Upload & Review** — upload to mainframe; show summary and repayments (Brief Step 9)
>   3. **Co-borrowers** — add co-borrowers or guarantors, existing or new (Brief Step 10, repeats 1–6)
>   4. **Submit** — checklist, then status New → Pending (Brief Step 11) → *Submit: loan saved as "Pending"* → Loan Details
> - Back on every step. Cancel returns to customer profile.
>
> Legend:
> - Dark arrows: main flow
> - Light arrows: co-borrower side path (Co-borrowers step → *Find or add a person* → Customer Search / New Customer Wizard → *Return to step 3*)
> - Log out is available on every screen
> - When adding a co-borrower, the new customer wizard returns to step 3 instead of the customer profile

Wizards are used for creating records, and the tabbed profile is used for viewing and editing them. The new customer wizard goes through steps 1 to 6 of the loan application process, while the new loan wizard covers steps 7 to 11. This allows an MMM to follow the bank's processes, without memorising them.

> **Figure 7:** Customer search. *(mock-up image — search for "Smith" returning a paginated list of 12 customers with name, customer number and date of birth)*

> **Figure 8:** Customer profile, Addresses tab. *(mock-up image — Mr John Smith's profile with Residential (Primary, Mailing), Postal and Holiday home addresses, and a note that a mailing address and at least one phone or email are needed before a new loan can start)*

> **Figure 9:** New customer wizard, step 2. *(mock-up image — Addresses step showing "Mailing address added" indicator)*

> **Figure 10:** New loan wizard, step 2. *(mock-up image — upload confirmation, loan summary with fortnightly repayment, principal, term, total interest and total cost, and the first rows of the repayment schedule)*

> **Figure 11:** Error handling on the Customer Details screen in LAVS. *(mock-up image — invalid date of birth "09/13/1978" highlighted in red with the message "Enter a real date, e.g. 09/01/1978")*

| Screen | Functionality Covered | Key Design Decisions |
|---|---|---|
| Login (not shown) | Log in. Retrieve constant values for categories (loaded once for all dropdowns). | Username and password, matching the mainframe's login. A failed login shows one general message, rather than naming the wrong field. |
| Header (every screen) | Log Out. | Logout sits in the account menu, so it is one click away from any screen. |
| Customer Search (Figure 7) | List existing customers; add a new customer. | One search box accepts a name or a customer number, replacing the terminal's separate "Load" and "Select" screens. A search button sends one request per search, rather than per keystroke. Name, customer number and date of birth tell customers with the same name apart. One action per row. Reused in a "pick" mode to add co-borrowers. |
| Customer Profile (Figure 8) | Retrieve and edit a customer; add, update and delete addresses, phones, emails and employers; list a customer's loans; customer documents and notes. | A summary card always shows whose record is open. Tabs show a count of records. Multiple addresses, with "Primary" and "Mailing" badges. Records are read-only until Edit, and the profile is the only place existing records are edited. |
| New Customer Wizard (Figures 9 & 11) | Add a new customer. | Five steps in the order the loan rules need: details, addresses, contacts, employment, review. Indicators confirm each rule is met, such as "Mailing address added". "Not employed" warns that a guarantor is needed. Step 1 validates each field inline and keeps valid input (Figure 11). Reuses the profile's form components. |
| New Loan Wizard (Figure 10) | Add a loan; retrieve the repayment summary and schedule; add co-borrowers; change a loan's status. | Step 1 validates each field before upload, with dropdowns filled from the mainframe's category values. Step 2 confirms the upload and shows the mainframe's repayment summary to review with the customer. Step 3 adds each person as Coborrower or Guarantor, matching the mainframe. Step 4 shows a checklist, then changes the status from New to Pending. |
| Loan Details (not shown) | Retrieve and edit loans; view summary and repayment schedule; add, update and delete co-borrowers; change status; loan documents and notes. | Same tabbed layout as the customer profile: Details, Co-borrowers, Summary & schedule, Documents, Notes. Status changes from New to Pending via a Submit button on the loan's summary card, which runs the same checklist as step 4 of the New Loan Wizard. |

*Table 4: LAVS screens, functionality covered and design decisions.*

Countrywide blue (#393A7B) is used for the header, as well as primary actions. Countrywide red (#D95733) is only used as a brand stripe, with a separate red being used for errors. Yellow (#F8F40C) is mostly unused, as it is hard to see against the light background.

A key assumption was made when developing these GUI mock-ups. Field lists and dropdown options follow the mainframe screen specifications, with placeholders for values that are unconfirmed.

## 8 Proposed Tests

The project will have many tests covering the business logic layer and the mainframe integration, with fewer tests covering integration on the UI layer, mainly using manual testing for UI components. The codebase will use JUnit 5, Mockito for mocking the mainframe interactions, and JaCoCo to show test coverage. JaCoCo will be configured in Maven to fail the build if line or branch coverage on the business logic layer falls below 95%. This and code-level checks (Checkstyle) will run through GitHub Actions on every PR, failing the build if a target is broken. Checkstyle will fail the build if a method exceeds a cyclomatic complexity of 10, nests conditions more than 3 deep, or uses single-letter variable names outside loop counters, enforcing the targets in Section 6. CK, an open-source tool that calculates object-oriented metrics from Java code, will also run on every PR. The pipeline will fail if the DIT or NOC targets are broken, and will attach a report of CBO, WMC, fan-in and fan-out for reviewers to compare across classes. Developers can also check their own code with the MetricsTree plugin in IntelliJ.

Equivalence and boundary value analysis will also test the validation rules. This will validate everything mentioned in table 1, section 3.2. A decision table will be used to test field requirements and the unemployed guarantor rule. Unit tests to any code which interacts with the mainframe will be applied to ensure round-trip data integrity. This will ensure that translating data from mainframe format to domain objects and back again does not lose or corrupt any data. Where enums are used, unknown category codes from the mainframe should also be handled.

ArchUnit will be used to enforce the dual layer requirement. This will ensure clean separation between the UI and business logic layer of the application, and it will cause a clear build failure if the UI layer contains any business logic or interacts with the mainframe.

To test the UI, a step-by-step walkthrough of each page will be performed, visually inspecting and processing through a written script of components to test, to ensure thorough testing of the UI. CI will run on a clean Windows runner through GitHub Actions on every PR.

## 9 Team Roles

Table 5 sets out each member's primary role; all members will contribute to development.

| Name | Role | Responsibility |
|---|---|---|
| Aidan Gleeson | Developer #1 | This person should drive programming of the UI and business logic layers and their components. They will work closely with the other developer. |
| Carson May | Developer #2 | This person will drive integration of the mainframe into the final product along with ensuring code quality through testing. They will work closely with the other developer. |
| Vikram Varadarajan | Product Manager | This person must have strong direction and ensure excellent communication between team members. They should regularly monitor timelines, scope, and progress of the team. |
| Jack Preston | UI/UX Designer | This person must design user interfaces and propose systems that add value to a user's experience of the final product. |

*Table 5: Team roles and their responsibilities linked with their assignees.*

## 10 Summary

In summary, this proposal presents a LAVS for Countrywide Banking Corporation. The aim is to create a final product that replaces the company's current text-based terminal screens whilst maintaining existing mainframe systems. To do this, the team will implement a user interface that MMMs can learn with minimal training. From a technical perspective, this product will be built using a modular layered approach. This includes the user interface layer that a user would directly interact with and a business logic layer handling the main functionality by communicating with the mainframe. This report described the domain model, validation rules, showed how data flows between the MMM, LAVS and mainframe, and proposed a GUI of different views. It also discussed measurable metrics, and devised a testing approach that uses a mocked connection and enforces the layer boundary. Together, these decisions produce a reliable loan application system.

## 11 Team Contributions

The following table lists each section of the report and its primary author.

| Section | Primary Author |
|---|---|
| Introduction | Collaborative |
| System Model | Vikram Varadarajan |
| Data Schema | Aidan Gleeson |
| Data Flows | Aidan Gleeson |
| Proposed Mainframe Integration | Carson May |
| Proposed Software Metrics | Jack Preston |
| Proposed GUI Mock-ups | Jack Preston |
| Proposed Tests | Carson May |
| Team Roles | Vikram Varadarajan |
| Summary | Collaborative |

*Table 6: Team Contribution Table.*
