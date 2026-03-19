# 🏦 Online Banking System

**Stack:** Spring Boot 4.0.2 · Java 17 · MySQL · Thymeleaf · Spring Data JPA · Lombok · MapStruct · OpenPDF · JavaMail · BCrypt

---

## 🔐 Security & Authentication

| Feature | Details |
|---|---|
| **BCrypt Password Hashing** | All passwords hashed on save; verified with `matches()` on login — no plain text stored |
| **Customer 2FA Login** | After correct password → OTP sent to email before session is created |
| **Email Verification on Registration** | Account inactive until OTP is verified |
| **OTP Resend with Cooldown** | Rate-limited resend via AJAX (`/customer/api/resend-otp`) |
| **OTP Expiry & Max Attempts** | OTP expires and locks after too many wrong tries |
| **Session-Based Auth** | Admin and Customer sessions managed via `HttpSession` |
| **Role-Based Admin Access** | Three roles: `MANAGER`, `ADMINISTRATIVE`, `DIRECTOR` — privilege-escalation guards on all endpoints |

---

## 👤 Customer Features

### Account Management
- Self-registration with Aadhaar + email uniqueness validation
- Email OTP verification before login is allowed
- Create **SAVINGS** or **CURRENT** bank account (one per customer)
- Account can be locked by admin — blocks all transactions
- Locked status displayed on customer dashboard with color indicator

### Transactions

| Type | Details |
|---|---|
| **Withdraw** | 6-digit PIN required, balance check, locked-account check |
| **Transfer** | PIN required, same-account prevention, deadlock-safe (sorted lock acquisition) |

- View full **transaction history** (DEPOSIT / WITHDRAW / TRANSFER) with sender/receiver names resolved

### Statements
- **Download PDF statement** (filterable by date range: `from` / `to`)
- **Email PDF statement** to self
- Professional PDF with header, account details, styled transaction table, balance summary

### Profile & Security
- Edit personal profile (name, email, phone, address)
- **Change Password** (verifies old BCrypt hash, stores new hash)
- **Change PIN** (6-digit transaction PIN)

---

## 🔩 Admin Features

### Admin Roles & Hierarchy

| Role | Permissions |
|---|---|
| `DIRECTOR` | Can create/manage ADMINISTRATIVE, MANAGER, and other Directors |
| `ADMINISTRATIVE` | Can create/manage MANAGER accounts only |
| `MANAGER` | Customer & account operations only |

### Customer & Account Operations
- View **All Customers** (AJAX-loaded table)
- View **All Accounts** (filterable by type: SAVINGS / CURRENT)
- View **All Transactions** (AJAX-loaded, all types)
- View individual **Customer Profile**
- View individual **Customer Account + Transaction History**
- **Lock / Unlock** customer accounts (AJAX)
- **Admin Deposit** into any customer account
- **Admin Withdraw** from any customer account
- **Admin Transact Page** — search customer by account number, deposit/withdraw with live results
- **Send PDF Statement** to customer's email (from account page or All Customers page)

### Admin Self-Management
- View and update own profile (name, email, phone, address, password)
- **Manage Admins** page — lists manageable admins based on role hierarchy
- **Edit / Delete** other admins (role-gated)
- View other admin profiles

### Dashboard
- Summary cards: Total Customers, Total Accounts, Total Transactions
- Navigation to all sub-pages

---

## 📦 Data Model

| Entity | Key Fields |
|---|---|
| `Customer` | fname, lname, email, adharcard, address, password *(BCrypt)*, phone, dob, pin, emailVerified |
| `Account` | accountNumber, balance, accountType (SAVINGS/CURRENT), branch, IFSC, locked, createdAt |
| `Transaction` | type (DEPOSIT/WITHDRAW/TRANSFER), amount, timestamp, remark, remainingBalance, senderAccountId, receiverAccountId |
| `Admin` | fname, lname, email, adharcard, password *(BCrypt)*, phone, dob, adminRole |
| `OtpEntity` | email, otp, otpType, expiry, attempts |

---

## 🖥️ UI Pages (Thymeleaf)

| Page | Route |
|---|---|
| Home | `/` |
| Customer Register | `/customer/register-customer` |
| Email Verify | `/customer/verify-email` |
| Customer Login | `/login-customer` |
| Login OTP (2FA) | `/customer/login-otp` |
| Customer Dashboard | `/customer/dashboard-customer` |
| Customer Profile | `/customer/profile` |
| Admin Login | `/login-admin` |
| Admin Dashboard | `/admin/dashboard-admin` |
| Admin Profile | `/admin/profile` |
| Register Admin | `/admin/register-admin` |
| Manage Admins | `/admin/manage-admins` |
| All Customers | `/admin/all-customers` |
| All Accounts | `/admin/all-accounts` |
| All Transactions | `/admin/all-transactions` |
| Admin Transact | `/admin/transact` |
| Customer Account (admin view) | `/admin/customer-account/{id}` |
| Customer Profile (admin view) | `/admin/customer-profile/{id}` |

---

## ⚙️ Infrastructure & Utilities

| Feature | Details |
|---|---|
| **Auto-Seed Admin** | Default `DIRECTOR` admin (`admin@bank.com`) created with BCrypt-hashed password on first startup via `DataInitializer` |
| **Swagger UI** | API docs auto-generated at `/swagger-ui.html` via SpringDoc OpenAPI |
| **PDF Generation** | `PdfStatementGenerator` utility using OpenPDF library |
| **Email Service** | `EmailServiceImpl` via Spring Mail (SMTP) — sends OTPs and PDF statements as attachments |
| **AJAX-first Admin UI** | All list tables loaded via `fetch()` calls to REST endpoints — no full page reloads |
| **Deadlock Prevention** | Fund transfers acquire account locks in sorted order to prevent race conditions |

---

## 📡 Key REST API Endpoints

### Admin APIs (`/admin/api/...`)
| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/admin/api/customers` | All customers (JSON) |
| GET | `/admin/api/accounts` | All accounts (JSON) |
| GET | `/admin/api/transactions` | All transactions (JSON) |
| GET | `/admin/api/transactions/deposit-withdraw` | DEPOSIT + WITHDRAW only |
| GET | `/admin/api/admins` | Manageable admins (role-gated) |
| GET | `/admin/api/admin/{id}` | Single admin by ID |
| GET | `/admin/api/customer-transactions/{email}` | Customer's transaction history |
| POST | `/admin/api/account/{id}/lock` | Lock account |
| POST | `/admin/api/account/{id}/unlock` | Unlock account |
| POST | `/admin/api/account/{id}/deposit` | Deposit into account |
| POST | `/admin/api/account/{id}/withdraw` | Withdraw from account |
| POST | `/admin/api/account/{id}/send-statement` | Email PDF statement to customer |

### Customer APIs (`/customer/api/...`)
| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/customer/api/account` | Own account info |
| GET | `/customer/api/transactions` | Own transaction history |
| POST | `/customer/api/transfer` | Fund transfer |
| POST | `/customer/api/withdraw` | Withdrawal |
| GET | `/customer/api/download-statement` | Download PDF statement |
| POST | `/customer/api/send-statement` | Email PDF statement to self |
| POST | `/customer/api/resend-otp` | Resend OTP (rate-limited) |
| POST | `/customer/api/change-password` | Change password |
| POST | `/customer/api/change-pin` | Change transaction PIN |
