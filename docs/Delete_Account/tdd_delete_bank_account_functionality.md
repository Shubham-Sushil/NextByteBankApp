# Technical Design Document (TDD)

**Feature Name:** Delete Bank Account Functionality  
**Project:** NextByteBankApp (`com.nextbyte.bank`)  
**Target Stack:** Java 17+, Jakarta Servlet API 5.0, Jakarta JSP 3.0, JSTL 2.0, HTML5/CSS3/Vanilla JS  
**Status:** Ready for Implementation  
**Date:** November 24, 2026  

---

## 1. Executive Summary & Architectural Overview

### 1.1. Purpose
This Technical Design Document translates the **Delete Bank Account PRD** into concrete backend and frontend implementation specifications tailored to the existing `NextByteBankApp` MVC architecture.

### 1.2. Current System Architecture
Currently, `NextByteBankApp` follows a layered Jakarta EE Servlet/JSP architecture backed by an in-memory synchronized repository:
* **Presentation Layer (`webapp/`):** `index.html`, `form.jsp`, `success.jsp`, `accountsList.jsp`, and `resources/css/style.css`.
* **Controller Layer (`com.nextbyte.bank.controller`):**
  * `AccountController` (`/openAccount`) — handles account creation (`POST`).
  * `ViewAccountsController` (`/viewAccounts`) — fetches `AccountRepository.getAllAccounts()` and forwards to `accountsList.jsp` (`GET`).
* **Manager & Service Layer (`manager/`, `service/`):** `AccountManager`, `BankService`, `AbstractBankService`, `HdfcAccountService`, and `SbiAccountService`.
* **Persistence Layer (`repository/`):** `AccountRepository` stores `BankAccount` instances inside a static `Collections.synchronizedList(new ArrayList<>())`.

### 1.3. Proposed High-Level Design
To preserve clean separation of concerns and support both **Confirmation Modal** and **Undo Toast** workflows without introducing heavy external dependencies:
1. **Asynchronous REST-style Servlet Endpoint (`DeleteAccountController`):** Introduce a dedicated `@WebServlet("/deleteAccount")` endpoint accepting `POST` / `DELETE` requests with `accountNumber` and returning JSON responses (`200 OK`, `400 Bad Request`, `404 Not Found`) so the UI can remove rows dynamically and render confirmation toasts without full-page reloads.
2. **Soft-Delete / Undo Buffer Support (Optional/Recommended):** Support immediate repository removal (`AccountRepository.deleteAccountByNumber(int)`) alongside an optional restore endpoint (`@WebServlet("/restoreAccount")`) or client-side cached payload to fulfill the 5-second **Undo Toast** workflow cleanly.
3. **JSP & Vanilla JS Enhancement (`accountsList.jsp` & `style.css`):** Add the `Actions` column, accessible confirmation modal, and bottom-right undo toast component directly to `accountsList.jsp` using lightweight, zero-dependency JavaScript (`fetch` API).

---

## 2. System Flow & Sequence Diagram

```
Admin / User                 accountsList.jsp (UI)           DeleteAccountController          AccountRepository
     |                                |                                 |                              |
     |--- Clicks [x] on Row 1001 ---->|                                 |                              |
     |                                |--- Opens Confirmation Modal     |                              |
     |--- Clicks [Delete] ----------->|                                 |                              |
     |                                |--- POST /deleteAccount -------->|                              |
     |                                |    (accountNumber=1001)         |                              |
     |                                |                                 |--- deleteByAccountNumber --->|
     |                                |                                 |<-- returns deleted Account --|
     |                                |<-- 200 OK { status: "deleted" }-|                              |
     |                                |--- Removes row from DOM         |                              |
     |                                |--- Shows Undo Toast (5s)        |                              |
     |                                |                                 |                              |
     |--- [Optional] Clicks [Undo] -->|                                 |                              |
     |                                |--- POST /restoreAccount ------->|                              |
     |                                |    (cached account payload)     |--- restoreAccount(account) ->|
     |                                |<-- 200 OK { status: "restored" }|                              |
     |                                |--- Re-inserts row into table    |                              |
```

---

## 3. Backend Component Design

### 3.1. Repository Layer (`com.nextbyte.bank.repository.AccountRepository`)
Currently, `AccountRepository` only provides `addAccount(BankAccount)` and `getAllAccounts()`. Because `accounts` is wrapped in `Collections.synchronizedList(...)`, compound iteration-and-removal operations **must** synchronize on the list monitor to prevent `ConcurrentModificationException`.

#### New Methods to Add:
```java
package com.nextbyte.bank.repository;

import com.nextbyte.bank.model.BankAccount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class AccountRepository {
    private static final List<BankAccount> accounts = Collections.synchronizedList(new ArrayList<>());

    public static void addAccount(BankAccount account) {
        accounts.add(account);
    }

    public static List<BankAccount> getAllAccounts() {
        synchronized (accounts) {
            return new ArrayList<>(accounts);
        }
    }

    /**
     * Finds and removes a BankAccount by its unique accountNumber.
     * @param accountNumber The unique 4-digit account ID (e.g., 1001)
     * @return Optional containing the removed BankAccount if found, otherwise empty
     */
    public static Optional<BankAccount> deleteAccountByNumber(int accountNumber) {
        synchronized (accounts) {
            Iterator<BankAccount> iterator = accounts.iterator();
            while (iterator.hasNext()) {
                BankAccount acc = iterator.next();
                if (acc.getAccountNumber() == accountNumber) {
                    iterator.remove();
                    return Optional.of(acc);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Restores a previously deleted account back into the repository (for Undo workflow),
     * maintaining ascending order by accountNumber.
     */
    public static boolean restoreAccount(BankAccount account) {
        if (account == null) return false;
        synchronized (accounts) {
            boolean exists = accounts.stream()
                    .anyMatch(a -> a.getAccountNumber() == account.getAccountNumber());
            if (exists) return false;

            accounts.add(account);
            accounts.sort((a, b) -> Integer.compare(a.getAccountNumber(), b.getAccountNumber()));
            return true;
        }
    }

    /**
     * Seeds the repository with a demo account so delete functionality can be verified
     * without first creating an account via the UI. Idempotent — only adds if missing.
     */
    public static void seedDemoAccount(BankAccount demoAccount) {
        if (demoAccount == null) return;
        synchronized (accounts) {
            boolean exists = accounts.stream()
                    .anyMatch(a -> a.getAccountNumber() == demoAccount.getAccountNumber());
            if (!exists) {
                accounts.add(demoAccount);
            }
        }
    }
}
```

### 3.2. Manager / Service Layer (`com.nextbyte.bank.manager.AccountManager`)
Add a clean delegation method in `AccountManager` so controllers do not bypass the manager layer when performing account lifecycle operations:

```java
// Addition to AccountManager.java
public Optional<BankAccount> deleteAccount(int accountNumber) {
    if (accountNumber <= 0) {
        throw new IllegalArgumentException("Invalid account number: " + accountNumber);
    }
    return AccountRepository.deleteAccountByNumber(accountNumber);
}

public boolean restoreAccount(BankAccount account) {
    return AccountRepository.restoreAccount(account);
}
```
*(Note: Add a no-arg constructor `public AccountManager() {}` to `AccountManager` alongside `public AccountManager(BankService bankService)` so deletion operations do not require instantiating a bank-specific creation service).*

### 3.3. Controller Layer (`com.nextbyte.bank.controller.DeleteAccountController`)
Create a new servlet mapped to `/deleteAccount` that handles both AJAX/Fetch JSON requests and standard HTML form fallback submissions.

* **URL Mapping:** `@WebServlet("/deleteAccount")`
* **HTTP Methods:** `POST` (primary, compatible with standard form submission & `fetch`) and `DELETE` (optional REST support).
* **Session-Backed Undo Cache:** Store the last deleted `BankAccount` in `HttpSession` under key `"lastDeletedAccount"` with a timestamp (`"lastDeletedAt"`) so `/restoreAccount` can securely restore the exact object without trusting raw client-modified balances.

#### API Contract: `POST /deleteAccount`
* **Request Parameters / Form URL-Encoded:**
  * `accountNumber` (required, integer, e.g., `1001`)
* **Responses (`application/json; charset=UTF-8`):**
  * **`200 OK` (Success):**
    ```json
    {
      "success": true,
      "message": "Account 1001 deleted successfully.",
      "accountNumber": 1001,
      "bankName": "HDFC Bank",
      "fullName": "Nextbyte"
    }
    ```
  * **`400 Bad Request` (Missing/Non-numeric `accountNumber`):**
    ```json
    {
      "success": false,
      "message": "Invalid or missing account number."
    }
    ```
  * **`404 Not Found` (Account already deleted or does not exist):**
    ```json
    {
      "success": false,
      "message": "Account 1001 was not found or has already been deleted."
    }
    ```

#### API Contract: `POST /restoreAccount` (Undo Action)
* **Request Parameters:**
  * `accountNumber` (required, integer)
* **Validation & Behavior:**
  * Checks `session.getAttribute("lastDeletedAccount")`.
  * Verifies `accountNumber` matches and elapsed time is within a 10-second server grace window.
  * Calls `accountManager.restoreAccount(cachedAccount)`, clears the session attribute, and returns `200 OK` so the UI can reload or re-insert the row.

---

## 4. Frontend & UI/UX Implementation (`accountsList.jsp` & `style.css`)

### 4.1. Table Structure Updates (`accountsList.jsp`)
1. **Table Header (`<thead>`):**
   * Append `<th class="col-actions">Actions</th>` after `<th>Opening Time</th>` (or as the leading/trailing column per finalized UI layout; trailing after `Opening Time` preserves natural reading order of `Acc No` first).
2. **Table Rows (`<tbody>`):**
   * Add `data-account-number="${acc.accountNumber}"` to each `<tr>` for O(1) DOM lookup upon deletion.
   * Add the action cell:
     ```jsp
     <td class="action-cell">
         <button type="button"
                 class="btn-delete-icon"
                 title="Delete Account"
                 aria-label="Delete ${acc.bankName} Account ${acc.accountNumber}"
                 onclick="openDeleteModal('${acc.accountNumber}', '${acc.bankName}', '${acc.fullName}', '${acc.balanceAmount}')">
             &#10005;
         </button>
     </td>
     ```
   * *(Security Note: Use JSTL `<c:out>` or `fn:escapeXml(...)` when rendering `acc.fullName` inside inline attributes to prevent Cross-Site Scripting (XSS) from user-entered names).*

### 4.2. Confirmation Modal Component
Embed a hidden modal dialog at the bottom of `accountsList.jsp` with ARIA accessibility attributes (`role="dialog"`, `aria-modal="true"`, `aria-labelledby="deleteModalTitle"`):

* **Visual Tokens (aligned with `style.css`):**
  * **Backdrop:** `rgba(15, 23, 42, 0.55)` with subtle fade-in transition.
  * **Card Container:** `#ffffff`, `border-radius: 10px`, `max-width: 420px`, `padding: 24px`, `box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.15)`.
  * **Dynamic Message:** *"Are you sure you want to delete **{Bank Name}** Account (**{Acc No}**)? This cannot be undone."*
  * **Balance Warning (Conditional):** If `balanceAmount > 0`, render a subtle amber badge inside the modal: *"Note: This account currently holds a balance of ₹{Balance}."*
  * **Buttons:**
    * `Cancel`: Background `#6b7280` (hover `#4b5563`), white text, dismisses modal.
    * `Delete`: Background `#dc2626` (hover `#b91c1c`), white text, triggers `confirmDeleteAccount()`.

### 4.3. Undo Toast Notification Component
* **Container:** Fixed bottom-right (`bottom: 24px; right: 24px; z-index: 1100;`), `#ffffff` card with `1px solid #e2e8f0`, green circular check icon (`#16a34a`), message text (`"Account 1002 deleted"`), and an outlined `Undo` button.
* **Lifecycle:**
  * Auto-dismisses after `5000ms` (`setTimeout`).
  * If a subsequent deletion occurs while a toast is active, clear the prior timer and update the toast with the latest deleted account.
  * Clicking `Undo` sends `POST /restoreAccount` and refreshes the table view (`/viewAccounts`).

### 4.4. Empty State Handling
When the final row in `<tbody>` is removed via JavaScript (or when `accountsList` is initially empty), display a clean fallback row spanning all 10 columns:
```html
<tr id="emptyStateRow">
    <td colspan="10" style="text-align: center; padding: 28px; color: #64748b;">
        No bank accounts found.
    </td>
</tr>
```

---

## 5. Edge Cases & Concurrency Handling

| Scenario | Handling Strategy |
| :--- | :--- |
| **Double-Click on Confirm Delete** | Disable both `Cancel` and `Delete` buttons immediately upon click, change label to `Deleting...`, and guard with an `isDeleting` boolean flag in JS. |
| **Concurrent Deletion (Two Tabs)** | `AccountRepository.deleteAccountByNumber` synchronizes on `accounts`. The second request safely receives `Optional.empty()` -> `404 Not Found`, and the UI removes the stale row gracefully with an informational message. |
| **XSS via Account Holder Name** | Escape `fullName` using `<c:out value="${acc.fullName}"/>` and read row metadata via `data-*` attributes (`dataset.accountNumber`, `dataset.fullName`, `dataset.bankName`) instead of string-interpolated `onclick` arguments. |
| **Page Refresh / Direct Navigation** | Because deletion mutates `AccountRepository` in memory, subsequent `GET /viewAccounts` requests reflect the updated list accurately. |
| **Keyboard Accessibility** | Pressing `Escape` closes the confirmation modal; opening the modal moves focus to the `Cancel` button by default to prevent accidental `Enter`-key confirmation. |

---

## 6. File Change Summary & Implementation Plan

| File Path | Action | Description |
| :--- | :--- | :--- |
| `src/main/java/com/nextbyte/bank/repository/AccountRepository.java` | **Modify** | Add thread-safe `deleteAccountByNumber(int)` and `restoreAccount(BankAccount)` methods. |
| `src/main/java/com/nextbyte/bank/manager/AccountManager.java` | **Modify** | Add no-arg constructor and `deleteAccount(int)` / `restoreAccount(BankAccount)` delegation methods. |
| `src/main/java/com/nextbyte/bank/controller/DeleteAccountController.java` | **Create** | Servlet handling `POST /deleteAccount` with JSON response and `HttpSession` undo caching. |
| `src/main/java/com/nextbyte/bank/controller/RestoreAccountController.java` | **Create** | Servlet handling `POST /restoreAccount` to revert the most recent deletion within the grace window. |
| `src/main/webapp/accountsList.jsp` | **Modify** | Add `Actions` column, `data-*` row attributes, Confirmation Modal HTML, Undo Toast HTML, and Fetch JS logic. |
| `src/main/webapp/resources/css/style.css` | **Modify** | Add styles for `.btn-delete-icon`, `.modal-backdrop`, `.modal-card`, and `.toast-undo`. |

---

## 7. Verification & Testing Checklist

1. **Unit / Repository Verification:**
   * Verify deleting an existing account (`1001`) reduces `AccountRepository.getAllAccounts().size()` by 1 and leaves remaining accounts intact.
   * Verify deleting a non-existent ID (`9999`) returns `Optional.empty()` without throwing exceptions.
2. **End-to-End UI Flow:**
   * Create 2 accounts (`HDFC` and `SBI`) via `form.jsp` -> navigate to `viewAccounts`.
   * Click `×` on row `1001` -> verify modal opens with `"HDFC Bank"` and `"1001"`, and focus lands on `Cancel`.
   * Click `Cancel` / press `Esc` -> verify modal closes and row `1001` remains.
   * Click `×` -> click `Delete` -> verify row `1001` disappears, toast `"Account 1001 deleted"` appears, and refreshing the page keeps `1001` deleted.
   * Click `Undo` on the toast within 5 seconds -> verify account `1001` is restored in sorted order.