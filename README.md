# VaultFlow - Full-Stack Enterprise Expense Tracker 💰📊

[![Java](https://img.shields.io/badge/Language-Java%2021-orange.svg)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Framework-Spring%20Boot%203.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Data JPA](https://img.shields.io/badge/ORM-Hibernate%20/%20JPA-blue.svg)](https://spring.io/projects/spring-data-jpa)
[![Security](https://img.shields.io/badge/Security-BCrypt%20Hashing-red.svg)](https://spring.io/projects/spring-security)
[![Database](https://img.shields.io/badge/Database-MySQL%20/%20H2-blue.svg)](https://www.mysql.com/)
[![Frontend](https://img.shields.io/badge/Frontend-HTML5%20/%20CSS3%20/%20Modern%20JS-yellow.svg)](#)
[![Charts](https://img.shields.io/badge/Analytics-Chart.js-ff6384.svg)](https://www.chartjs.org/)

A full-stack, responsive financial management and expense tracking platform built with **Java 21**, **Spring Boot 3**, **Spring Data JPA**, **MySQL**, and modern **JavaScript / Chart.js**.

---

## 🚀 Key Features

### 🔐 1. Authentication & Security
- **BCrypt Password Hashing**: Passwords are cryptographically hashed and salted before database persistence.
- **Self-Healing Credential Migration**: Transparently upgrades any legacy plain-text passwords into BCrypt on first login.
- **Input Validation**: Centralized Jakarta Validation (`@Valid`, `@NotBlank`, `@Size`, `@Email`, `@Positive`).
- **Profile & Credential Management**: Update user profile details and securely change account passwords.

### 💳 2. Transaction Management
- **Income & Expense Tracking**: Strict typing with `TransactionType.INCOME` and `TransactionType.EXPENSE`.
- **Predefined & Custom Categories**: Food & Dining, Housing, Utilities, Shopping, Transportation, Entertainment, Healthcare, Salary, Investments, and General.
- **Full CRUD Capabilities**: Add, edit, filter, and delete transactions with confirmation.
- **Search & Multi-Facet Filtering**: Filter dynamically by keyword/search term, transaction type, category, and date range (`startDate`, `endDate`).
- **CSV Data Export**: Instant RFC-4180 compliant CSV export for spreadsheets and accounting software.

### 📊 3. Interactive Analytics & Dashboard
- **Dynamic Financial KPIs**:
  - Total Current Balance ($) = Total Income - Total Expenses
  - Total Income & Total Expenses
  - Monthly Cash Flow & Net Monthly Savings
  - Monthly Savings Rate (%)
- **Interactive Chart.js Visualizations**:
  - **Cash Flow Comparison**: Income vs Expenses bar chart.
  - **Expense Distribution**: Interactive category breakdown doughnut chart.
  - **6-Month Financial Trend**: Dual-axis income and expenditure line chart.
- **Spend Intelligence**:
  - Automatically identifies top spending category.
  - Computes historical average monthly expenditure and lifetime net savings.

### 🎯 4. Budget Tracking & Overspending Alerts
- **Monthly Category Spending Limits**: Set target spending limits per category for each month.
- **Live Utilization Tracking**: Dynamic progress bars and percentage calculation.
- **Visual Alert Banners**:
  - `NORMAL`: Below 80% utilization.
  - `WARNING`: 80% to 100% utilization.
  - `EXCEEDED`: Over 100% spending limit.

---

## 🏗️ Architecture & Layered Design

```
Client Browser (HTML5 / CSS3 / ES6 / Chart.js)
                 │
                 ▼  REST API (JSON / Multipart)
┌────────────────────────────────────────────────────────┐
│                      Spring Boot                       │
│  ┌──────────────────────────────────────────────────┐  │
│  │                   Web Layer                      │  │
│  │   • UserController       • TransactionController │  │
│  │   • BudgetController     • AnalyticsController   │  │
│  │   • ExportController     • GlobalExceptionHandler│  │
│  └────────────────────────┬─────────────────────────┘  │
│                           │                            │
│  ┌────────────────────────▼─────────────────────────┐  │
│  │                  Service Layer                   │  │
│  │   • UserService          • TransactionService    │  │
│  │   • BudgetService        • AnalyticsService      │  │
│  │   • SecurityConfig (BCryptPasswordEncoder)       │  │
│  └────────────────────────┬─────────────────────────┘  │
│                           │                            │
│  ┌────────────────────────▼─────────────────────────┐  │
│  │                 Repository Layer                 │  │
│  │   • UserRepository       • TransactionRepository │  │
│  │   • BudgetRepository     (Spring Data JPA)       │  │
│  └────────────────────────┬─────────────────────────┘  │
└───────────────────────────┼────────────────────────────┘
                            │
                            ▼
             Database (MySQL / H2 In-Memory)
```

---

## 🗄️ Database Schema & Entity Relationships

```
┌──────────────────┐               ┌──────────────────┐
│      users       │ 1           * │   transaction    │
├──────────────────┼───────────────┼──────────────────┤
│ id (PK)          │               │ id (PK)          │
│ name             │               │ user_id (FK)     │
│ username (UNIQUE)│               │ description      │
│ email (UNIQUE)   │               │ amount           │
│ password (BCRYPT)│               │ type (ENUM)      │
│ created_at       │               │ category         │
│ updated_at       │               │ date             │
└────────┬─────────┘               │ created_at       │
         │                         └──────────────────┘
         │ 1
         │
         │ *
┌────────┴─────────┐
│     budgets      │
├──────────────────┤
│ id (PK)          │
│ user_id (FK)     │
│ category         │
│ monthly_limit    │
│ budget_month     │
│ budget_year      │
│ created_at       │
└──────────────────┘
```

---

## 📁 Final Project Directory Structure

```
expense/
├── pom.xml                                           # Maven dependencies & build configuration
├── README.md                                         # Complete architectural & operational guide
├── src/
│   ├── main/
│   │   ├── java/com/example/Expense/Tracker/
│   │   │   ├── ExpenseTrackerApplication.java        # Spring Boot main entry point
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java               # BCrypt bean configuration
│   │   │   │   └── WebConfig.java                    # Global CORS configuration
│   │   │   ├── controller/
│   │   │   │   ├── UserController.java               # Auth, registration, profile & passwords
│   │   │   │   ├── TransactionController.java        # Transaction CRUD & multi-facet filtering
│   │   │   │   ├── BudgetController.java             # Budget limits & progress calculations
│   │   │   │   ├── AnalyticsController.java          # Dashboard KPIs, trends & breakdown
│   │   │   │   └── ExportController.java             # CSV transaction export
│   │   │   ├── dto/
│   │   │   │   ├── auth/                             # Auth & user request/response DTOs
│   │   │   │   ├── transaction/                      # Transaction DTOs
│   │   │   │   ├── budget/                           # Budget DTOs
│   │   │   │   ├── analytics/                        # Dashboard summary & chart DTOs
│   │   │   │   └── common/                           # Standard ApiResponse & ErrorResponse
│   │   │   ├── exception/
│   │   │   │   ├── BadRequestException.java          # HTTP 400
│   │   │   │   ├── ResourceNotFoundException.java    # HTTP 404
│   │   │   │   ├── UnauthorizedException.java        # HTTP 401
│   │   │   │   └── GlobalExceptionHandler.java       # Centralized REST exception advice
│   │   │   ├── model/
│   │   │   │   ├── User.java                         # User entity
│   │   │   │   ├── Transaction.java                  # Transaction entity
│   │   │   │   ├── Budget.java                       # Budget entity
│   │   │   │   └── TransactionType.java              # Enum (INCOME, EXPENSE)
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── TransactionRepository.java        # Aggregations & multi-predicate search
│   │   │   │   └── BudgetRepository.java
│   │   │   └── service/
│   │   │       ├── UserService.java
│   │   │       ├── TransactionService.java
│   │   │       ├── TransactionServiceImpl.java
│   │   │       ├── BudgetService.java
│   │   │       └── AnalyticsService.java
│   │   └── resources/
│   │       ├── application.properties                # Main application properties
│   │       ├── application-mysql.properties          # Production MySQL configuration
│   │       └── application-h2.properties             # Development & testing in-memory DB
│   └── test/
│       └── java/com/example/Expense/Tracker/
│           └── ExpenseTrackerApplicationTests.java   # Automated integration test suite
└── Expense-Tracker-Frontend/
    ├── login.html                                    # Modern login with 1-click demo button
    ├── register.html                                 # Registration page with validation
    ├── expense_tracker.html                          # Complete SaaS Dashboard application
    ├── styles.css                                    # Design system, CSS variables & layouts
    └── script.js                                     # Reactive UI, Chart.js lifecycle, CRUD
```

---

## ⚙️ Setup and Execution Instructions

### Prerequisites
- **Java**: JDK 17 or 21 (`java -version`)
- **Maven**: 3.8+ (`mvn -version`)
- **Database**: MySQL 8.0+ or embedded H2

### 1. Database Configuration
By default, the application connects to MySQL on port `3306` (or `3307`). You can configure credentials via environment variables:

```bash
export DB_URL="jdbc:mysql://localhost:3306/expense_tracker?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export DB_USERNAME="root"
export DB_PASSWORD="your_password"
```

To run with zero-config in-memory database (no MySQL required):
```bash
export SPRING_PROFILES_ACTIVE=h2
```

### 2. Start the Backend
From the project root:

```bash
mvn spring-boot:run
```
The Spring Boot backend will start on **`http://localhost:8080`**.

### 3. Run the Automated Tests
```bash
mvn test
```
All 6 integration tests run against an in-memory database to verify the full user, transaction, budget, and calculation lifecycle.

### 4. Open the Frontend
You can open `Expense-Tracker-Frontend/login.html` directly in your browser, or serve it using Python:

```bash
python3 -m http.server 3000 --directory Expense-Tracker-Frontend
```
Then visit **`http://localhost:3000/login.html`** in your browser.

---

## 🔑 Demo Account
For rapid testing and evaluation:
- **Username:** `sarah`
- **Password:** `Password123`
*(Or click the **"Use Demo"** button on `login.html` to autofill!)*

---

## 📖 REST API Documentation

### 1. Authentication (`/ExpTrack/auth` & `/ExpTrack`)
| Method | Endpoint | Description | Request Body |
|---|---|---|---|
| `POST` | `/ExpTrack/auth/register` | Register new user | `{"name":"...","username":"...","email":"...","password":"..."}` |
| `POST` | `/ExpTrack/auth/login` | Login user | `{"username":"...","password":"..."}` |
| `GET` | `/ExpTrack/users/profile/{username}` | Get user profile & stats | None |
| `PUT` | `/ExpTrack/users/profile/{username}` | Update user name & email | `{"name":"...","email":"..."}` |
| `PUT` | `/ExpTrack/users/password/{username}`| Change password | `{"currentPassword":"...","newPassword":"..."}` |

### 2. Transactions (`/ExpTrack/transactions`)
| Method | Endpoint | Description | Parameters / Body |
|---|---|---|---|
| `POST` | `/ExpTrack/transactions/{username}` | Add transaction | `{"description":"...","amount":100,"type":"EXPENSE","category":"Food & Dining","date":"2026-11-13"}` |
| `GET` | `/ExpTrack/transactions/{username}` | Get transactions (with filters)| `?type=EXPENSE&category=Food&startDate=...&endDate=...&keyword=...` |
| `GET` | `/ExpTrack/transactions/{username}/{id}` | Get single transaction | Path variable `id` |
| `PUT` | `/ExpTrack/transactions/{username}/{id}` | Edit transaction | Same as POST body |
| `DELETE`| `/ExpTrack/transactions/{username}/{id}` | Delete transaction | Path variable `id` |

### 3. Budgets (`/ExpTrack/budgets`)
| Method | Endpoint | Description | Parameters / Body |
|---|---|---|---|
| `POST` | `/ExpTrack/budgets/{username}` | Set / update budget | `{"category":"Housing","monthlyLimit":2000,"month":11,"year":2026}` |
| `GET` | `/ExpTrack/budgets/{username}` | Get budgets & utilization | `?month=11&year=2026` |
| `DELETE`| `/ExpTrack/budgets/{username}/{id}` | Delete budget limit | Path variable `id` |

### 4. Analytics & Reports (`/ExpTrack/analytics`)
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/ExpTrack/analytics/dashboard/{username}` | Total balance, monthly cash flows, savings rate, recent transactions, and active budget alerts |
| `GET` | `/ExpTrack/analytics/reports/{username}` | Multi-month trend series, category share breakdown, and spending stats |
| `GET` | `/ExpTrack/analytics/categories/{username}` | Category breakdown with date ranges |

### 5. CSV Export (`/ExpTrack/export`)
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/ExpTrack/export/csv/{username}` | Download CSV export of all or filtered transactions |
