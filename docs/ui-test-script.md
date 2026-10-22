<!-- AI-assisted: written with Claude from the proposal; reviewed by Carson May. -->
# UI test script

This script checks the UI against the proposal: the screen flow (Figure 6), the screens and their design decisions (Table 4), the validation rules (Table 1, Section 3.2), how saves are reported (Figure 5, Section 4.3) and the loan application process (Table 2).

Where the code knowingly departs from the proposal, test against [deviations.md](deviations.md) instead.
Anything else that doesn't match the expected result is a failure. Log it, and either fix the code or add a deviation.

## How to run the script

1. Start the app with `./mvnw clean javafx:run` (Windows: `.\mvnw.cmd clean javafx:run`).
3. Work through the sections in order. Later sections use the customers and loan created in earlier ones.
4. For each row, do the action, compare with the expected result, and mark the last column pass, fail or N/A (with a reason in the defect log).
5. For every fail add a row to the [defect log](#defect-log) with what you saw, and a screenshot if it's visual.

A row passes only if *everything* in its expected result is true.
Unless a row says otherwise, "error" means a message in red under the field, with the field highlighted, and every other value in the form left as it was (proposal Figure 11).


### Test data

The course mainframe is shared with other teams, so make every name unique to this run.
Pick a **run tag** of your initials and the time, e.g. `CM1432`, and use it as the family name of every customer you create.

| Customer | First name | Used for |
|---|---|---|
| **A** | Alice | The primary applicant. Employed, full contact details. |
| **B** | Ben | A second applicant who is **not employed**, so their loan needs a guarantor. |
| **C** | Cara | Added to A's loan as a co-borrower. Created from inside the new loan wizard. |
| **G** | Greg | Added to B's loan as a guarantor. |

Have two small files ready to record as documents, e.g. `payslip.pdf` and `licence.jpg`.

---

## 1. Login

Proposal: Table 4 (Login), Figure 4 process 1.0.

| ID | Action | Expected result | P/F |
|---|---|---|---|
| LOG-01 | Start the app. | Login screen shows "Sign in", Username and Password fields, a Sign in button, and "Authorised Countrywide Bank staff only". No other screen can be reached. | |
| LOG-02 | Leave both fields empty and press **Sign in**. | An error under each empty field. Nothing is sent (the button never shows "Signing in…"). | |
| LOG-03 | Username `test`, password `wrong`, press **Sign in**. | While waiting, the button reads "Signing in…", the fields and button are disabled and a progress bar shows. Then one general message above the fields ("The user name or password is incorrect."), not naming which field was wrong. Password is cleared; username is kept. | |
| LOG-04 | Type a password and press the eye button, then press it again. | The password is shown as text, then hidden again, with the same value. | |
| LOG-05 | Username `test`, password `test`, press **Sign in**. | The app opens on **Customer search**. The header shows "LAVS", the account button shows the signed-in user. | |
| LOG-06 | Open any dropdown in the app (e.g. **+ Add customer** → Title). | Options are filled (constants were loaded once at login). No visible delay or loading when opening it. | |

## 2. Header and log out

Proposal: Table 4 (Header), Figure 6 legend ("Log out is available on every screen").
Repeat LOG-OUT-01 on each screen as you reach it in later sections, and tick the screens off here.

| ID | Action | Expected result | P/F |
|---|---|---|---|
| HDR-01 | Look at the header on Customer search. | Header is Countrywide blue (#393A7B) with a red (#D95733) brand stripe. Yellow is not used. Error messages elsewhere use a different red from the stripe. | |
| HDR-02 | Click the account button. | A menu opens directly below it with **Log out**. Clicking the button again closes it. | |
| LOG-OUT-01 | Choose **Log out**. Tick each screen once checked: ☐ Customer search ☐ Customer profile ☐ New customer wizard ☐ Loan details ☐ New loan wizard | Returns to the login screen with a "Signed out" message. Username and password are empty. | |
| LOG-OUT-02 | After logging out, sign in again. | Opens on Customer search, not on the screen you left. | |

## 3. Customer search (first look)

Proposal: Table 4 (Customer search), Figure 7, Brief Step 1.

| ID | Action | Expected result | P/F |
|---|---|---|---|
| SRCH-01 | Look at the screen before searching. | "Find a customer", one search box ("Search by name or customer number"), a **Search** button and **+ Add customer**. A prompt to search, not an empty table. | |
| SRCH-02 | Type your run tag slowly, without pressing Search. | No search runs while typing (results don't change per keystroke). | |
| SRCH-03 | Press **Search**. | "No customers found. Check the spelling or search by customer number." (No customer has your run tag yet.) | |

## 4. New customer wizard: customer A

Proposal: Table 4 (New customer wizard), Figures 9 and 11, Table 1, Brief Steps 1–6.

### 4.1 Wizard frame

| ID | Action | Expected result | P/F |
|---|---|---|---|
| NCW-01 | On Customer search, press **+ Add customer**. | Wizard titled "New customer", "Not yet saved to the mainframe". Stepper shows five steps in order: Details, Addresses, Contacts, Employment, Review & save. Step 1 is current. Left button reads **Cancel**; right button reads **Next: Addresses →**. | |
| NCW-02 | Type a first name, then press **Cancel**. | Asks "Discard this new customer?" with **Keep going** and **Discard**. | |
| NCW-03 | Press **Keep going**. | The dialog closes and the first name you typed is still there. | |
| NCW-04 | Press **Cancel**, then **Discard**. Then press **+ Add customer** again. | Returns to Customer search. The new wizard is empty. | |

### 4.2 Step 1: Details

Each row starts from valid values in every other field, so you can see the other values are kept.

| ID | Action | Expected result | P/F |
|---|---|---|---|
| NCW-10 | Leave everything empty and press **Next**. | Errors under First name and Family name (deviation 1: family name is required). Title, Date of birth and Nationality are marked "(optional)" and have no error. Stays on step 1. | |
| NCW-11 | Open Title and Nationality. | Both are dropdowns (no free text). | |
| NCW-12 | First name: 41 characters. Family name: your run tag. Press **Next**. | Error under First name only: 40 characters or fewer. Family name kept. | |
| NCW-13 | First name: exactly 40 characters. | No error on First name. | |
| NCW-14 | Family name: 41 characters. | Error under Family name: 40 characters or fewer. | |
| NCW-15 | Date of birth: type `13/13/1978`. | Error "Enter a real date, e.g. 09/01/1978." Everything else kept. | |
| NCW-16 | Date of birth: today's date. Then tomorrow's date. | Both rejected: must be in the past. | |
| NCW-17 | Date of birth: use the date picker to choose a date in 1985. | Accepted. The field shows the date as dd/mm/yyyy. | |
| NCW-18 | Fix every field: Title Ms, First name **Alice**, Family name run tag, DOB 1985, Nationality New Zealand. Press **Next: Addresses →**. | Moves to step 2. Stepper shows step 1 as done (✓). Left button now reads **← Back**. | |
| NCW-19 | Press **← Back**, then **Next** again. | Step 1 still shows every value entered. | |

### 4.3 Step 2: Addresses

| ID | Action | Expected result | P/F |
|---|---|---|---|
| NCW-20 | Press **Next** with no addresses. | Stays on step 2 with a banner: "Add an address and mark it as the mailing address before moving on." "✓ Mailing address added" is not shown. | |
| NCW-21 | Press **+ Add address**, then **Save** with every field empty. | Pop-up stays open. Errors under Address type, Address line 1 and Country. Line 2, Suburb, City and Post code are "(optional)". | |
| NCW-22 | Address type: Other. | A "Describe the address" field appears. Saving with it empty gives an error (deviation 4). | |
| NCW-23 | Address line 1: 61 characters. | Error: 60 characters or fewer. | |
| NCW-24 | Post code: `12A4`. | Error: must contain only digits. | |
| NCW-25 | Post code: `0600`, save, then look at the saved address. | Saved as `0600`, with the leading zero (deviation 2). | |
| NCW-26 | Add a Residential address, line 1 `27 Kowhai Street`, City Auckland, Country New Zealand, **Primary** ticked, **Mailing** not ticked. Save. | Address listed with a "Primary" badge. Banner still blocks Next, because there is no mailing address. | |
| NCW-27 | Add a second address (Postal, `PO Box 1187`) with **Mailing** ticked. | Listed with a "Mailing" badge. "✓ Mailing address added" now shows. | |
| NCW-28 | Edit the postal address and tick **Primary**. Save. | Only one address shows "Primary" afterwards (at most one primary), or saving is refused with a clear message. Note which. | |
| NCW-29 | Edit an address, change line 1, press **Cancel**. | The address is unchanged. | |
| NCW-30 | Press **Next: Contacts →**. | Moves to step 3. | |

### 4.4 Step 3: Contacts

| ID | Action | Expected result | P/F |
|---|---|---|---|
| NCW-40 | Press **Next** with no contacts. | "No phone numbers or email addresses added yet." Banner: "Add a phone number or an email address before moving on." Stays on step 3. | |
| NCW-41 | **+ Add phone**, Save with every field empty. | Errors under Phone type, Country code and Number. | |
| NCW-42 | Phone type: Other. | "Describe the phone" appears and is required (deviation 4). | |
| NCW-43 | Add a Mobile phone, country code `64`, number `21 555 0142`, **Primary** and **Can receive texts** ticked. | Listed as Mobile, primary, can text. "✓ Phone or email added" shows. | |
| NCW-44 | **+ Add email**, Save with every field empty. | Errors under Email type (deviation 5) and Email address. | |
| NCW-45 | Email address: `alice.example.com`, then `alice@example`. | Both rejected: must be in the format name@example.com. | |
| NCW-46 | Email address: 61 characters ending in `@example.com`. | Error: 60 characters or fewer. | |
| NCW-47 | Add a Personal email `alice@example.co.nz`, **Primary** ticked. Then add a second email, also **Primary**. | Only one email shows as primary afterwards, or the second is refused with a clear message. | |
| NCW-48 | Press **Next: Employment →**. | Moves to step 4. | |

### 4.5 Step 4: Employment

| ID | Action | Expected result | P/F |
|---|---|---|---|
| NCW-50 | Look at the step. | Choice of **Employed** / **Not employed**. | |
| NCW-51 | Choose **Employed**, press Next with no employer. | Banner: "Add the customer's employer, or choose "Not employed"." | |
| NCW-52 | **+ Add employer**, Save with every field empty. | Errors under Employer type and Employer name. Job title and Salary are "(optional)". | |
| NCW-53 | Salary: `78k`. | Error "Enter the salary as a number, e.g. 78000." | |
| NCW-54 | Add employer: type Full time (or similar), name `Papamoa Primary School`, job title Teacher, salary `78,000`. | Listed with the job title and "$78,000 a year". | |
| NCW-55 | Press **Next: Review & save →**. | Moves to step 5. | |

### 4.6 Step 5: Review & save

| ID | Action | Expected result | P/F |
|---|---|---|---|
| NCW-60 | Look at the review. | "Ready to save". Checklist shows Customer details, Mailing address and Phone or email as met. Summary lists name, date of birth, nationality, addresses, contacts and employer exactly as entered. Right button reads **✓ Save customer**. | |
| NCW-61 | Press **Change** next to one section. | Goes back to that step with values kept. Stepper does not let you jump forward past steps you haven't completed. | |
| NCW-62 | Return to Review and press **✓ Save customer**. Watch the screen while it saves. | Save button is disabled and a progress indicator shows; the window can still be moved and redrawn (Figure 5). | |
| NCW-63 | Wait for the save to finish. | "Customer saved" confirmation. The **Customer profile** for Alice opens, with a customer number from the mainframe, and every address, contact and employer from the wizard. | |

## 5. Customers B, C and G

Create these with the wizard, as in Section 4, but more quickly. Only the rows below need checking.

| ID | Action | Expected result | P/F |
|---|---|---|---|
| NCW-70 | Create **Ben** (run tag) with one mailing address and only an email (no phone). At Employment choose **Not employed**. | Employer list is hidden or empty. The step warns that any loan will need a guarantor. Next is allowed with no employer. | |
| NCW-71 | Review Ben. | Employment shows "Not employed · any loan will need a guarantor". Contacts accepted with an email only. | |
| NCW-72 | Save Ben. | Profile opens. | |
| NCW-73 | Create **Greg** (run tag), employed, with mailing address and phone. | Saved; profile opens. | |
| — | Do **not** create Cara yet. She is created from the new loan wizard in Section 8. | | |

## 6. Customer search (with results)

| ID | Action | Expected result | P/F |
|---|---|---|---|
| SRCH-10 | Search for your run tag. | Results title shows the search term and a count. Columns: Name, Customer number, Date of birth. Alice, Ben and Greg are listed. One action per row (opening it). | |
| SRCH-11 | Search for Alice's customer number. | Exactly Alice is found. The same box accepts a name or a number. | |
| SRCH-12 | Search for a common surname, e.g. `Smith`. | If more results than fit on one page: pager shows "Page 1 of N"; **‹ Prev** is disabled on page 1; **Next ›** moves to page 2; **Next ›** is disabled on the last page. | |
| SRCH-13 | In the Smith results, find two customers with the same name. | They can be told apart by customer number and date of birth. | |
| SRCH-14 | Search for `zzqqxx`. | "No customers found. Check the spelling or search by customer number." | |
| SRCH-15 | Search for your run tag and open **Alice**. | Alice's Customer profile opens. | |

## 7. Customer profile: Alice

Proposal: Table 4 (Customer profile), Figure 8, Brief Steps 2–7.

### 7.1 Summary card and tabs

| ID | Action | Expected result | P/F |
|---|---|---|---|
| PRO-01 | Look at the top of the profile. | Summary card shows initials, "Ms Alice <run tag>", a status badge (Active), customer number, date of birth and suburb, and a **+ New loan** button. | |
| PRO-02 | Look at the tabs. | Details, Addresses, Contacts, Employment, Loans, Documents, Notes. Each record tab shows its count (e.g. Addresses 2). | |
| PRO-03 | Press **‹ Back to search results**. | Returns to Customer search with the previous results still showing. Open Alice again. | |
| PRO-04 | Switch between every tab. | The summary card stays on screen on every tab. | |

### 7.2 Details tab

| ID | Action | Expected result | P/F |
|---|---|---|---|
| PRO-10 | Open Details. | All fields are read-only text, not inputs. An **Edit** button is shown. | |
| PRO-11 | Press **Edit**, clear First name and Family name, press **Save**. | Pop-up stays open with errors under both names; nothing saved. | |
| PRO-12 | Set Date of birth to tomorrow, press **Save**. | Error: must be in the past. Other fields kept. | |
| PRO-13 | Change Nationality and press **Cancel**. | No change on the Details tab. | |
| PRO-14 | Press **Edit**, change Title to Dr, press **Save**. | "Details saved". Details tab and summary card both show "Dr Alice…". | |
| PRO-15 | Go back to search, search again and reopen Alice. | Title is still Dr (the change was saved to the mainframe, not just the screen). | |
| PRO-16 | Use the status link to **Make inactive**. | Asks for confirmation, explaining details, loans and history stay on record. On confirm: "Status changed"; badge shows Inactive. | |
| PRO-17 | **Make active** again. | Asks for confirmation; badge back to Active. Reopen the customer to confirm it stuck. | |

### 7.3 Addresses tab

| ID | Action | Expected result | P/F |
|---|---|---|---|
| PRO-20 | Open Addresses. | Both addresses from the wizard, with "Primary" and "Mailing" badges where set. Info note says a mailing address and a phone or email are needed before a new loan. | |
| PRO-21 | **+ Add address**: add a third address (Other, "Holiday home", Mount Maunganui). | The pop-up uses the same form as the wizard (same fields, same errors). Saved address appears; tab count goes up by 1. | |
| PRO-22 | Edit the holiday home, set Post code to `abc`, Save. | Error under Post code; pop-up stays open; other values kept. | |
| PRO-23 | Fix the post code and Save. | Change shown. Reopen the customer to confirm it was saved. | |
| PRO-24 | Edit the holiday home and press **Remove**. | Address removed; tab count goes down by 1. | |
| PRO-25 | If more than one page of addresses: use the pager. | Pager works as in SRCH-12. (N/A if too few addresses.) | |

### 7.4 Contacts tab

| ID | Action | Expected result | P/F |
|---|---|---|---|
| PRO-30 | Open Contacts. | Separate "Phone numbers" and "Email addresses" lists, each with its own add button and pager. | |
| PRO-31 | Add a second phone (Work). Edit it and mark it Primary. | Only one phone is primary afterwards, or the change is refused with a clear message. | |
| PRO-32 | Remove the work phone. | Removed; count updated. | |
| PRO-33 | Open **Ben**'s profile (email only). Edit his only email and press **Remove**. | Refused: "A customer needs at least one phone number or email address, so the last one cannot be deleted." The email stays. | |

### 7.5 Employment tab

| ID | Action | Expected result | P/F |
|---|---|---|---|
| PRO-40 | On Alice, open Employment. | Papamoa Primary School, with job title and salary. | |
| PRO-41 | Edit the employer, clear the name, Save. | Error under Employer name. | |
| PRO-42 | Change the salary to `82000` and Save. | Shows "$82,000 a year". Reopen the customer to confirm. | |
| PRO-43 | On Ben, open Employment. | "No employers recorded." | |

### 7.6 New loan button

| ID | Action | Expected result | P/F |
|---|---|---|---|
| PRO-50 | Create a throwaway customer (run tag, first name `Temp`) and, on their profile, remove the mailing flag from every address (Edit → untick Mailing → Save). Press **+ New loan**. | Does not open the wizard. Warning "Not ready for a loan yet" naming what's missing (a mailing address) and pointing to the Addresses and Contacts tabs. | |
| PRO-51 | On Alice, press **+ New loan**. | The New loan wizard opens for Alice. Continue with Section 8. | |

Documents and Notes tabs are tested in Section 10, after the loan exists, because the same components appear on both screens.

## 8. New loan wizard: Alice's loan

Proposal: Table 4 (New loan wizard), Figure 6, Figure 10, Table 1 (Loan, Co-borrower), Brief Steps 7–11.

### 8.1 Wizard frame

| ID | Action | Expected result | P/F |
|---|---|---|---|
| NLW-01 | Look at the wizard. | Titled "New loan" for Alice. Four steps: Loan details, Upload & review, Co-borrowers, Submit. Left button **Cancel**. | |
| NLW-02 | Enter a principal, press **Cancel**, then **Keep going**. | "Discard this new loan?" dialog; Keep going returns with the principal still entered. | |
| NLW-03 | Press **Cancel** → **Discard**. | Returns to Alice's **customer profile** (Figure 6: "Cancel returns to customer profile"). Press **+ New loan** again. | |

### 8.2 Step 1: Loan details

| ID | Action | Expected result | P/F |
|---|---|---|---|
| NLW-10 | Press **Next** with every field empty. | "All fields are required". Errors under all eight fields: Principal, Rate type, Interest rate, Rate applies, Repayment amount, Repayment frequency, Start date, Renewal date. | |
| NLW-11 | Open Rate type, Rate applies and Repayment frequency. | All dropdowns, filled with the mainframe's values (e.g. Fixed/Floating, Weekly/Fortnightly/Monthly). | |
| NLW-12 | Principal `0`. | Error: must be greater than 0. | |
| NLW-13 | Principal `450k`. | "Enter the amount as a number, e.g. 450000." | |
| NLW-14 | Principal `0.01`. | Accepted. Then set it to `450,000` (or `450000`). | |
| NLW-15 | Interest rate `-0.01`. | Error: must be 0 or more. | |
| NLW-16 | Interest rate `0`. | Accepted. Then set it to `6.29`. | |
| NLW-17 | Interest rate `six`. | "Enter the rate as a number, e.g. 6.29." | |
| NLW-18 | Repayment amount `abc`. | "Enter the repayment as a number, e.g. 1312.14." Then set `1312.14`. | |
| NLW-19 | Start date `31/02/2027`. | "Enter a real date, e.g. 01/11/2026." | |
| NLW-20 | Start date 01/12/2026 and Renewal date 01/12/2026 (same day). | Error under Renewal date: "Renewal date must be after the start date." | |
| NLW-21 | Renewal date 01/12/2028. Rate type Fixed, Rate applies Yearly, Repayment frequency Fortnightly. Press **Next**. | Moves to step 2. Every earlier error has cleared and no value was lost along the way. | |

### 8.3 Step 2: Upload & review

| ID | Action | Expected result | P/F |
|---|---|---|---|
| NLW-30 | Arrive at step 2. | The loan is sent to the mainframe (`loan.create`): Save disabled and progress shown while sending, then a confirmation with the new **loan number** (Figure 10, Brief Step 9). | |
| NLW-31 | Look at the summary. | The mainframe's loan summary: repayment amount and frequency, principal, term, total interest and total cost, matching the details entered. | |
| NLW-32 | Look at the repayment schedule. | First rows of the repayment schedule, with due dates, repayment, principal, interest and balance. | |
| NLW-33 | Press **Change** on the loan details, alter the repayment amount, return. | Summary and schedule reflect the change. | |
| NLW-34 | Press **Next: Co-borrowers →**. | Moves to step 3. | |

> **Note for the tester:** the proposal has step 2 upload the loan and show the mainframe's summary and schedule.
> If instead the loan is only sent at step 4, and step 2 shows supporting documents and the entered details, mark NLW-30 to NLW-33 as **F** and log it, unless a deviation has been added for it.

### 8.4 Step 3: Co-borrowers

| ID | Action | Expected result | P/F |
|---|---|---|---|
| NLW-40 | Look at the step. | "Co-borrowers and guarantors". Alice listed as "Primary applicant". Buttons **⌕ Find existing customer** and **+ Add new customer**. | |
| NLW-41 | **⌕ Find existing customer**. | Search box ("Search by name or customer number") and a choice to add them as **Co-borrower** or **Guarantor**, with an explanation of the difference. | |
| NLW-42 | Search for Alice and try to add her. | Refused: "The loan's own customer cannot also be a co-borrower." | |
| NLW-43 | Press **+ Add new customer**. | The New customer wizard opens (same five steps). | |
| NLW-44 | Create **Cara** (run tag), employed, with mailing address and phone. Save. | Returns to **step 3 of the new loan wizard**, not to Cara's profile (Figure 6 legend). Loan details entered earlier are unchanged. | |
| NLW-45 | Add Cara to the loan as **Co-borrower**. | "Added to the loan". Cara listed with a "Co-borrower" badge. | |
| NLW-46 | Find Cara again and try to add her a second time. | Refused: "This customer is already on the loan." | |
| NLW-47 | Remove Cara, then add her again as Co-borrower. | Asks to confirm ("They will no longer be on the loan…"). Removed, then re-added (changes are delete and re-add, Table 1). | |
| NLW-48 | Press **Next: Submit →**. | Moves to step 4. Alice is employed, so no guarantor is demanded. | |

### 8.5 Step 4: Submit

| ID | Action | Expected result | P/F |
|---|---|---|---|
| NLW-50 | Look at the step. | "Ready to submit". Checklist and summary: loan details, repayments, documents, and people on the loan (Alice, with 1 co-borrower). Right button **✓ Submit loan**. | |
| NLW-51 | Press **✓ Submit loan**. | Progress shown and the button disabled while sending. "Loan submitted". The **Loan details** screen opens with status **Pending** (Brief Step 11). | |
| NLW-52 | Go back to Alice's profile, Loans tab. | The loan is listed with loan number, principal, repayment and status Pending. Loans tab count is 1. | |

### 8.6 Ben's loan (guarantor rule)

| ID | Action | Expected result | P/F |
|---|---|---|---|
| NLW-60 | Open Ben's profile, press **+ New loan**, complete steps 1–2 with valid values. | Reaches step 3. | |
| NLW-61 | At step 3, look before adding anyone. | Banner: "The applicant isn't employed, so this loan needs a guarantor." Ben shown as "Not employed". | |
| NLW-62 | Press **Next** with no guarantor. | Blocked; stays on step 3 with the guarantor message. | |
| NLW-63 | Add **Greg** as **Co-borrower** and press Next. | Still blocked: a co-borrower does not satisfy the rule. | |
| NLW-64 | Remove Greg and add him as **Guarantor**. Press Next. | Allowed. Submit step shows Greg as Guarantor, "Needed because" Ben is not employed. | |
| NLW-65 | Press **← Back** to step 3, then **Cancel** from step 1 and **Discard**. | Returns to Ben's profile. (Ben's loan is not needed again; if the loan was already created at step 2, note what happens to it.) | |

## 9. Loan details screen

Proposal: Table 4 (Loan details). Reach it from Alice's profile → Loans tab → open the loan.

To test editing, you also need a loan still in **New**. Create one for Greg: profile → **+ New loan**, fill steps 1–2, then leave the wizard after the loan has been created. If the wizard doesn't create the loan before Submit (see the NLW-30 note), mark the New-loan rows N/A.

### 9.1 Layout

| ID | Action | Expected result | P/F |
|---|---|---|---|
| LD-01 | Open Alice's loan. | Back link "‹ Back to Alice <run tag>". Summary card: loan number, Pending badge, "for Alice…", principal, interest. Tiles: Total to repay (over N payments), Total interest, Repayments (amount, frequency, start date), Final payment. | |
| LD-02 | Look at the tabs. | Details, Schedule, Co-borrowers, Documents, Notes, the same tab style as the customer profile, with counts. | |
| LD-03 | Open **Schedule**. | Table with #, Due, Repayment, Principal, Interest, Balance after. Pager works. Values match the summary tiles (e.g. number of payments). | |
| LD-04 | Press the back link. | Returns to Alice's profile. | |

### 9.2 Pending loan is locked

| ID | Action | Expected result | P/F |
|---|---|---|---|
| LD-10 | On Alice's Pending loan, open Details. | No **Edit** button. Note: "This loan is pending, so its details can no longer be changed." | |
| LD-11 | Open Co-borrowers. | Cara listed. No add or remove actions, with a similar locked note. | |
| LD-12 | Look at the summary card. | No **Submit for approval** button (only New → Pending is allowed, Table 1). | |

### 9.3 New loan can be edited and submitted

| ID | Action | Expected result | P/F |
|---|---|---|---|
| LD-20 | Open Greg's New loan. | Status badge New. **Submit for approval** button on the summary card. Details tab has **Edit**, with a note that details can change until submitted. | |
| LD-21 | **Edit**, set Renewal date before Start date, Save. | Error under Renewal date; nothing saved; other values kept. | |
| LD-22 | **Edit**, change principal, Save. | Summary card, tiles and schedule update. Reopen the loan to confirm it was saved. | |
| LD-23 | Co-borrowers tab: add Alice as co-borrower, then remove her. | Works as in NLW-45 and NLW-47. | |
| LD-24 | Press **Submit for approval**. | Confirmation: "Submit this loan for approval?" "It will move to Pending, and its details can no longer be changed." | |
| LD-25 | Confirm. | Status badge Pending. Submit button gone. Details and co-borrowers locked as in 9.2. Reopen to confirm it stuck. | |

## 10. Documents and notes

Proposal: optional components (customer documents, customer notes, loan documents, loan notes).
Run every row twice: once on **Alice's profile** and once on **Alice's loan**, and mark both columns.

| ID | Action | Expected result | Profile | Loan |
|---|---|---|---|---|
| DOC-01 | Open the Documents tab with nothing recorded. | "No documents recorded." Info note: LAVS records the file's name, type and size; the file stays with the customer's paperwork. | | |
| DOC-02 | **+ Add document**, Save with nothing chosen. | "Document type is required." and "Choose the file to record." | | |
| DOC-03 | Open Document type. | Dropdown: Proof of income, Identification, Proof of address, Bank statement, KiwiSaver, Other. | | |
| DOC-04 | **Choose file…**, pick `payslip.pdf`, type Proof of income, Save. | Listed with file name, type and size (e.g. "PDF · 184 KB"). Tab count +1. Button changed to "Choose another…" while the file was chosen. | | |
| DOC-05 | Add `licence.jpg` as Identification. Edit it and change the type to Proof of address. Save. | Shown with the new type. Reopen the screen to confirm it was saved (deviation 12: a loan document gets a new id). | | |
| DOC-06 | Edit `licence.jpg` and **Remove** it. | Removed; count −1. Reopen to confirm. | | |
| NOTE-01 | Open the Notes tab with nothing recorded. | "No notes recorded." Info note: shared with all staff, newest first. | | |
| NOTE-02 | **+ Add note**, Save with nothing entered. | "Note type is required." and "Write the note." (deviation 13). | | |
| NOTE-03 | Type General, text "Called customer about payslip." Save. | Note shows type, text, author (the signed-in user) and today's date. | | |
| NOTE-04 | Add a second note, type Contact. | The new note is listed first. | | |
| NOTE-05 | Edit the first note's text and Save. Reopen the screen. | Change kept. | | |
| NOTE-06 | Remove a note. | Removed; count −1. Reopen to confirm. | | |
| DN-01 | Compare the profile's and the loan's Documents and Notes tabs. | Same layout, forms, messages and buttons (shared components, Table 3 reusability). | | |

## 11. Errors and responsiveness

Proposal: Figure 5, Section 4.3, Section 5.

| ID | Action | Expected result | P/F |
|---|---|---|---|
| ERR-01 | On Alice's profile, open **+ Add address**, fill it in, then **disconnect from the network** and press Save. | A connection error is shown in the pop-up; the pop-up stays open with every value kept. The app does not freeze. | |
| ERR-02 | Reconnect and press Save again. | Saves normally. | |
| ERR-03 | Disconnect, then press **Search** on Customer search. | A clear connection error, not an empty "No customers found". Reconnect and search again: works. | |
| ERR-04 | Start a new customer wizard, fill every step, disconnect, press **✓ Save customer**. | Error shown; the wizard stays open with every value kept, so you can retry without re-entering. If some parts were saved before the failure, the message says which were saved and which weren't. | |
| ERR-05 | Reconnect and Save again. | If part was saved already, no duplicate customer is created, or the message told you what to do about it. Note what happened. | |
| ERR-06 | Disconnect, then sign out and try to sign in. | "Could not reach the mainframe" (or equivalent); fields kept except the password. | |
| ERR-07 | Throughout the script, watch every save and search. | Every mainframe call shows progress and disables the button that started it; the window never stops responding; nothing hangs without a message. | |
| ERR-08 | Read every error, warning and info message you've seen. | Short, plain English; says what's wrong and how to fix it; no codes, stack traces or field names like `firstName`. | |

## 12. General usability

Proposal: Section 5, Section 7 ("recognition rather than recall", "error prevention").

| ID | Action | Expected result | P/F |
|---|---|---|---|
| GEN-01 | Across the whole script. | Every action can be done with a labelled button or link. No step needs a keyboard shortcut or memorised code. | |
| GEN-02 | Across every form. | Every fixed list of values (titles, nationalities, types, frequencies) is a dropdown. Every date has a date picker. | |
| GEN-03 | Across every form. | An invalid field never clears the rest of the form. | |
| GEN-04 | Press Tab through a form (e.g. the address pop-up). | Focus moves through the fields in reading order. | |
| GEN-05 | Resize the window to small and to maximised on Customer search, the profile, both wizards and Loan details. | Nothing is cut off with no way to reach it; scrolling appears where needed. | |
| GEN-06 | Check required and optional fields on every form. | Optional fields are marked "(optional)"; required ones aren't. | |
| GEN-07 | Hover or click each **ⓘ**. | Each shows a short, relevant explanation. | |

## 13. End to end: the brief's loan application process

Proposal: Table 2. A final run through the brief's eleven steps in one go, to check the screens connect.
Use a new customer (run tag, first name `Eve`) who doesn't exist yet.

| ID | Brief step | Action | Expected result | P/F |
|---|---|---|---|---|
| E2E-01 | 1 | Search for Eve; she isn't found. **+ Add customer**. | Wizard opens. | |
| E2E-02 | 2–5 | Enter details, a mailing address, and a phone and an email. | Indicators confirm the mailing address and contact rules. | |
| E2E-03 | 6 | Add an employer. | Accepted. | |
| E2E-04 | 1–6 | **✓ Save customer**. | Profile opens with a customer number. | |
| E2E-05 | 7–8 | **+ New loan**, enter the loan details. | Each field validated inline. | |
| E2E-06 | 9 | Step 2. | Loan number, summary and repayment schedule from the mainframe. | |
| E2E-07 | 10 | Add Cara as co-borrower. | Listed on the loan. | |
| E2E-08 | 11 | Submit. | Loan Pending; Loan details opens. | |
| E2E-09 | — | Log out, sign in, search for Eve, open the loan. | Everything above is still there, so it was saved to the mainframe. | |

---

## Results summary

| Section | Passed | Failed | N/A |
|---|---|---|---|
| 1. Login | | | |
| 2. Header and log out | | | |
| 3. Customer search (first look) | | | |
| 4. New customer wizard | | | |
| 5. Customers B, C and G | | | |
| 6. Customer search (with results) | | | |
| 7. Customer profile | | | |
| 8. New loan wizard | | | |
| 9. Loan details | | | |
| 10. Documents and notes | | | |
| 11. Errors and responsiveness | | | |
| 12. General usability | | | |
| 13. End to end | | | |
| **Total** | | | |

## Defect log

| # | Test ID | What happened | Expected | Screenshot | Issue / PR |
|---|---|---|---|---|---|
| 1 | | | | | |
