/**
 * VaultFlow - India-First Personal Finance Application Logic
 * Currency: INR (₹) | Locale: en-IN | Timezone: Asia/Kolkata | Format: DD-MM-YYYY
 */

// Dynamically resolve API_BASE for both local dev and cloud deployment (Render, Railway, etc.)
const API_BASE = (window.location.protocol === "file:" || window.location.port === "3000")
  ? "http://localhost:8080/ExpTrack"
  : `${window.location.origin}/ExpTrack`;

// Verify Authentication
const username = localStorage.getItem("user");
if (!username) {
  window.location.href = "login.html";
}

// Global Chart Instances
let incomeExpenseChartInstance = null;
let categoryChartInstance = null;
let trendChartInstance = null;

// Application State
let currentSelectedType = "EXPENSE"; // Default for new transactions
let currentTab = "dashboard";

// --------------------------------------------------------------------------
// Reusable Currency & Date Formatting Utilities (en-IN)
// --------------------------------------------------------------------------
const inrFormatter = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
  maximumFractionDigits: 2,
});

function formatCurrency(amount) {
  return inrFormatter.format(amount || 0);
}

function formatDateDisplay(dateStr) {
  if (!dateStr) return "";
  const parts = dateStr.split("-");
  if (parts.length === 3) {
    return `${parts[2]}-${parts[1]}-${parts[0]}`; // DD-MM-YYYY
  }
  return dateStr;
}

// Toast Notification Helper
function showToast(message, type = "info") {
  const container = document.getElementById("toast-container");
  if (!container) return;

  const toast = document.createElement("div");
  toast.className = `toast toast-${type}`;
  const icons = {
    success: "✅",
    error: "⚠️",
    warning: "🔔",
    info: "ℹ️",
  };

  toast.innerHTML = `
    <span>${icons[type] || "ℹ️"}</span>
    <div style="flex: 1;">${message}</div>
  `;

  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = "0";
    toast.style.transform = "translateX(40px)";
    toast.style.transition = "all 0.3s ease";
    setTimeout(() => toast.remove(), 300);
  }, 3500);
}

// --------------------------------------------------------------------------
// Indian Categories Data Structure
// --------------------------------------------------------------------------
const INDIAN_EXPENSE_CATEGORIES = [
  {
    group: "Essential Expenses",
    items: [
      "Groceries",
      "Vegetables & Fruits",
      "Rent",
      "Electricity",
      "Water Bill",
      "Gas Cylinder",
      "Internet & Mobile Recharge",
      "Transportation",
      "Fuel",
      "Medical & Healthcare",
      "Education",
    ],
  },
  {
    group: "Lifestyle",
    items: [
      "Restaurants & Dining",
      "Food Delivery",
      "Shopping",
      "Entertainment",
      "Travel",
      "Gym & Fitness",
      "Personal Care",
      "Subscriptions",
    ],
  },
  {
    group: "Financial",
    items: [
      "EMI & Loan Payments",
      "Insurance",
      "Investments",
      "Savings",
      "Bank Charges",
      "Taxes",
    ],
  },
];

const INDIAN_INCOME_CATEGORIES = [
  {
    group: "Income Sources",
    items: [
      "Salary",
      "Freelancing",
      "Business Income",
      "Internship Stipend",
      "Scholarship",
      "Interest Income",
      "Rental Income",
      "Investment Returns",
      "Other Income",
    ],
  },
];

function populateCategoryDropdown(type, selectedCategory = "") {
  const select = document.getElementById("txCategorySelect");
  const customGroup = document.getElementById("customCategoryGroup");
  const customInput = document.getElementById("txCustomCategory");
  select.innerHTML = "";

  const list = type === "INCOME" ? INDIAN_INCOME_CATEGORIES : INDIAN_EXPENSE_CATEGORIES;
  let found = false;

  list.forEach((grp) => {
    const optgroup = document.createElement("optgroup");
    optgroup.label = grp.group;
    grp.items.forEach((item) => {
      const opt = document.createElement("option");
      opt.value = item;
      opt.textContent = item;
      if (item.toLowerCase() === (selectedCategory || "").toLowerCase()) {
        opt.selected = true;
        found = true;
      }
      optgroup.appendChild(opt);
    });
    select.appendChild(optgroup);
  });

  // Custom Category option
  const customOpt = document.createElement("option");
  customOpt.value = "__CUSTOM__";
  customOpt.textContent = "+ Custom Category...";
  select.appendChild(customOpt);

  if (selectedCategory && !found) {
    customOpt.selected = true;
    customGroup.style.display = "block";
    customInput.value = selectedCategory;
  } else {
    customGroup.style.display = "none";
    customInput.value = "";
  }
}

// --------------------------------------------------------------------------
// Navigation & Tab Switching
// --------------------------------------------------------------------------
function switchTab(tabId) {
  currentTab = tabId;

  document.querySelectorAll(".nav-item").forEach((item) => {
    item.classList.toggle("active", item.getAttribute("data-tab") === tabId);
  });

  document.querySelectorAll(".tab-pane").forEach((pane) => {
    pane.classList.toggle("active", pane.id === `tab-${tabId}`);
  });

  const headings = {
    dashboard: "Dashboard Overview",
    transactions: "Transaction History",
    analytics: "Financial Analytics & Reports",
    budgets: "Monthly Budgets",
    profile: "Account Profile & Settings",
  };
  document.getElementById("pageHeading").textContent = headings[tabId] || "Dashboard";

  if (tabId === "dashboard") fetchDashboard();
  if (tabId === "transactions") fetchTransactions();
  if (tabId === "analytics") fetchAnalytics();
  if (tabId === "budgets") fetchBudgets();
  if (tabId === "profile") fetchProfile();

  document.getElementById("sidebar").classList.remove("mobile-open");
}

document.querySelectorAll(".nav-item").forEach((item) => {
  item.addEventListener("click", () => {
    const tab = item.getAttribute("data-tab");
    switchTab(tab);
  });
});

document.getElementById("mobileMenuBtn").addEventListener("click", () => {
  document.getElementById("sidebar").classList.toggle("mobile-open");
});

document.getElementById("logoutBtn").addEventListener("click", () => {
  localStorage.clear();
  window.location.href = "login.html";
});

// --------------------------------------------------------------------------
// Data Fetching: Dashboard
// --------------------------------------------------------------------------
async function fetchDashboard() {
  try {
    const res = await fetch(`${API_BASE}/analytics/dashboard/${username}`);
    const result = await res.json();

    if (!res.ok) throw new Error(result.message || "Failed to load dashboard data");
    const data = result.data;

    // Update KPIs (Formatted in INR)
    document.getElementById("kpiBalance").textContent = formatCurrency(data.totalBalance);
    document.getElementById("kpiIncome").textContent = formatCurrency(data.totalIncome);
    document.getElementById("kpiExpense").textContent = formatCurrency(data.totalExpense);

    document.getElementById("kpiMonthIncome").textContent = `This month: ${formatCurrency(data.thisMonthIncome)}`;
    document.getElementById("kpiMonthExpense").textContent = `This month: ${formatCurrency(data.thisMonthExpense)}`;

    const savingsRate = data.savingsRate || 0;
    document.getElementById("kpiSavingsRate").textContent = `${savingsRate.toFixed(1)}%`;
    document.getElementById("kpiTxCount").textContent = `${data.totalTransactionCount} Transactions`;

    const netBadge = document.getElementById("kpiNetBadge");
    if (data.totalBalance >= 0) {
      netBadge.className = "kpi-badge positive";
      netBadge.textContent = "Positive Balance";
    } else {
      netBadge.className = "kpi-badge negative";
      netBadge.textContent = "Negative Balance";
    }

    renderBudgetAlerts(data.budgetAlerts);
    renderRecentTransactions(data.recentTransactions);
    renderIncomeExpenseChart(data.totalIncome, data.totalExpense);
    renderCategoryChart(data.categoryBreakdown);

  } catch (err) {
    console.error("Dashboard error:", err);
    showToast(err.message, "error");
  }
}

function renderBudgetAlerts(alerts) {
  const container = document.getElementById("budgetAlertContainer");
  container.innerHTML = "";

  if (!alerts || alerts.length === 0) return;

  alerts.forEach((alert) => {
    const banner = document.createElement("div");
    const isExceeded = alert.status === "EXCEEDED";
    banner.className = `alert-banner ${isExceeded ? "alert-danger" : "alert-warning"}`;

    banner.innerHTML = `
      <div class="alert-content">
        <span style="font-size: 20px;">${isExceeded ? "🚨" : "⚠️"}</span>
        <div>
          <strong>${alert.category} Budget Alert:</strong> 
          Spent ${formatCurrency(alert.spent)} of ${formatCurrency(alert.monthlyLimit)} limit (${alert.percentageUsed}% consumed).
        </div>
      </div>
      <button class="btn btn-sm ${isExceeded ? "btn-danger" : "btn-secondary"}" onclick="switchTab('budgets')">
        Manage Budget
      </button>
    `;
    container.appendChild(banner);
  });
}

function renderRecentTransactions(transactions) {
  const tbody = document.getElementById("recentTransactionsList");
  tbody.innerHTML = "";

  if (!transactions || transactions.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="7" class="empty-state">
          <div class="empty-icon">💸</div>
          <p>No recent transactions recorded.</p>
        </td>
      </tr>
    `;
    return;
  }

  transactions.forEach((tx) => {
    const tr = document.createElement("tr");
    const isExpense = tx.type === "EXPENSE";

    let paymentBadge = escapeHtml(tx.paymentMethod || "UPI");
    if (tx.upiApp) {
      paymentBadge += ` • ${escapeHtml(tx.upiApp)}`;
    }

    tr.innerHTML = `
      <td>${formatDateDisplay(tx.date)}</td>
      <td><strong>${escapeHtml(tx.description || tx.text)}</strong></td>
      <td><span class="badge-category">🏷️ ${escapeHtml(tx.category || "General")}</span></td>
      <td><span class="badge-category" style="background:#e0f2fe; color:#0369a1;">📱 ${paymentBadge}</span></td>
      <td><span class="kpi-badge ${isExpense ? "negative" : "positive"}">${tx.type}</span></td>
      <td class="tx-amount ${isExpense ? "tx-expense" : "tx-income"}">
        ${isExpense ? "-" : "+"}${formatCurrency(tx.amount)}
      </td>
      <td>
        <div class="tx-actions">
          <button class="btn-icon" title="Edit" onclick="openEditTxModal(${JSON.stringify(tx).replace(/"/g, '&quot;')})">✏️</button>
          <button class="btn-icon delete" title="Delete" onclick="deleteTransaction(${tx.id})">🗑️</button>
        </div>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

// --------------------------------------------------------------------------
// Charts Rendering (INR & en-IN Locale)
// --------------------------------------------------------------------------
function renderIncomeExpenseChart(income, expense) {
  const canvas = document.getElementById("incomeExpenseChart");
  if (!canvas) return;
  const ctx = canvas.getContext("2d");

  if (incomeExpenseChartInstance) {
    incomeExpenseChartInstance.destroy();
  }

  incomeExpenseChartInstance = new Chart(ctx, {
    type: "bar",
    data: {
      labels: ["Total Income", "Total Expenses"],
      datasets: [
        {
          label: "Amount (₹)",
          data: [income || 0, expense || 0],
          backgroundColor: ["#10b981", "#ef4444"],
          borderRadius: 8,
          barThickness: 50,
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        tooltip: {
          callbacks: {
            label: (context) => ` ${formatCurrency(context.parsed.y)}`,
          },
        },
      },
      scales: {
        y: {
          beginAtZero: true,
          grid: { color: "#f1f5f9" },
          ticks: {
            callback: (value) => formatCurrency(value),
          },
        },
        x: {
          grid: { display: false },
        },
      },
    },
  });
}

function renderCategoryChart(categories) {
  const canvas = document.getElementById("categoryBreakdownChart");
  if (!canvas) return;
  const ctx = canvas.getContext("2d");

  if (categoryChartInstance) {
    categoryChartInstance.destroy();
  }

  if (!categories || categories.length === 0) {
    categoryChartInstance = new Chart(ctx, {
      type: "doughnut",
      data: {
        labels: ["No Expenses Yet"],
        datasets: [{ data: [1], backgroundColor: ["#e2e8f0"] }],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { position: "bottom" } },
      },
    });
    return;
  }

  const labels = categories.map((c) => c.category);
  const data = categories.map((c) => c.amount);
  const palette = [
    "#6366f1", "#10b981", "#f59e0b", "#ef4444", "#0ea5e9",
    "#8b5cf6", "#ec4899", "#14b8a6", "#f97316", "#64748b",
    "#06b6d4", "#84cc16", "#d946ef"
  ];

  categoryChartInstance = new Chart(ctx, {
    type: "doughnut",
    data: {
      labels: labels,
      datasets: [
        {
          data: data,
          backgroundColor: palette.slice(0, labels.length),
          borderWidth: 2,
          borderColor: "#ffffff",
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: "bottom",
          labels: { boxWidth: 12, padding: 10, font: { size: 11 } },
        },
        tooltip: {
          callbacks: {
            label: (context) => ` ${context.label}: ${formatCurrency(context.raw)}`,
          },
        },
      },
      cutout: "68%",
    },
  });
}

// --------------------------------------------------------------------------
// Transactions Management Tab
// --------------------------------------------------------------------------
async function fetchTransactions() {
  const keyword = document.getElementById("txSearchInput").value.trim();
  const type = document.getElementById("txTypeFilter").value;
  const category = document.getElementById("txCategoryFilter").value;
  const startDate = document.getElementById("txStartDate").value;
  const endDate = document.getElementById("txEndDate").value;

  const params = new URLSearchParams();
  if (keyword) params.append("keyword", keyword);
  if (type) params.append("type", type);
  if (category) params.append("category", category);
  if (startDate) params.append("startDate", startDate);
  if (endDate) params.append("endDate", endDate);

  try {
    const res = await fetch(`${API_BASE}/transactions/${username}?${params.toString()}`);
    const transactions = await res.json();

    const tbody = document.getElementById("allTransactionsList");
    tbody.innerHTML = "";

    if (!transactions || transactions.length === 0) {
      tbody.innerHTML = `
        <tr>
          <td colspan="7" class="empty-state">
            <div class="empty-icon">🔍</div>
            <p>No transactions match your search criteria.</p>
          </td>
        </tr>
      `;
      return;
    }

    transactions.forEach((tx) => {
      const tr = document.createElement("tr");
      const isExpense = tx.type === "EXPENSE";

      let paymentBadge = escapeHtml(tx.paymentMethod || "UPI");
      if (tx.upiApp) {
        paymentBadge += ` • ${escapeHtml(tx.upiApp)}`;
      }

      tr.innerHTML = `
        <td>${formatDateDisplay(tx.date)}</td>
        <td><strong>${escapeHtml(tx.description || tx.text)}</strong></td>
        <td><span class="badge-category">🏷️ ${escapeHtml(tx.category || "General")}</span></td>
        <td><span class="badge-category" style="background:#e0f2fe; color:#0369a1;">📱 ${paymentBadge}</span></td>
        <td><span class="kpi-badge ${isExpense ? "negative" : "positive"}">${tx.type}</span></td>
        <td class="tx-amount ${isExpense ? "tx-expense" : "tx-income"}">
          ${isExpense ? "-" : "+"}${formatCurrency(tx.amount)}
        </td>
        <td>
          <div class="tx-actions">
            <button class="btn-icon" title="Edit" onclick="openEditTxModal(${JSON.stringify(tx).replace(/"/g, '&quot;')})">✏️</button>
            <button class="btn-icon delete" title="Delete" onclick="deleteTransaction(${tx.id})">🗑️</button>
          </div>
        </td>
      `;
      tbody.appendChild(tr);
    });

  } catch (err) {
    console.error("Fetch transactions error:", err);
    showToast(err.message, "error");
  }
}

document.getElementById("txSearchInput").addEventListener("input", debounce(fetchTransactions, 300));
document.getElementById("txTypeFilter").addEventListener("change", fetchTransactions);
document.getElementById("txCategoryFilter").addEventListener("change", fetchTransactions);
document.getElementById("txStartDate").addEventListener("change", fetchTransactions);
document.getElementById("txEndDate").addEventListener("change", fetchTransactions);

document.getElementById("resetFiltersBtn").addEventListener("click", () => {
  document.getElementById("txSearchInput").value = "";
  document.getElementById("txTypeFilter").value = "";
  document.getElementById("txCategoryFilter").value = "";
  document.getElementById("txStartDate").value = "";
  document.getElementById("txEndDate").value = "";
  fetchTransactions();
});

// --------------------------------------------------------------------------
// Add / Edit Transaction Modal
// --------------------------------------------------------------------------
const txModal = document.getElementById("txModal");
const typeExpenseBtn = document.getElementById("typeExpenseBtn");
const typeIncomeBtn = document.getElementById("typeIncomeBtn");
const txPaymentMethodSelect = document.getElementById("txPaymentMethod");
const upiAppGroup = document.getElementById("upiAppGroup");
const txCategorySelect = document.getElementById("txCategorySelect");
const customCategoryGroup = document.getElementById("customCategoryGroup");

function setTransactionType(type, categoryToSelect = "") {
  currentSelectedType = type;
  if (type === "EXPENSE") {
    typeExpenseBtn.className = "type-toggle-btn active expense";
    typeIncomeBtn.className = "type-toggle-btn";
  } else {
    typeIncomeBtn.className = "type-toggle-btn active income";
    typeExpenseBtn.className = "type-toggle-btn";
  }
  populateCategoryDropdown(type, categoryToSelect);
}

typeExpenseBtn.addEventListener("click", () => setTransactionType("EXPENSE"));
typeIncomeBtn.addEventListener("click", () => setTransactionType("INCOME"));

// Toggle UPI App field
txPaymentMethodSelect.addEventListener("change", (e) => {
  if (e.target.value === "UPI") {
    upiAppGroup.style.display = "block";
  } else {
    upiAppGroup.style.display = "none";
  }
});

// Toggle Custom Category field
txCategorySelect.addEventListener("change", (e) => {
  if (e.target.value === "__CUSTOM__") {
    customCategoryGroup.style.display = "block";
    document.getElementById("txCustomCategory").focus();
  } else {
    customCategoryGroup.style.display = "none";
  }
});

document.getElementById("openAddTxModalBtn").addEventListener("click", () => {
  document.getElementById("txForm").reset();
  document.getElementById("editTxId").value = "";
  document.getElementById("txModalTitle").textContent = "Add Transaction";
  document.getElementById("txDate").value = new Date().toISOString().split("T")[0];
  document.getElementById("txPaymentMethod").value = "UPI";
  upiAppGroup.style.display = "block";
  setTransactionType("EXPENSE");
  txModal.classList.add("active");
});

document.getElementById("closeTxModalBtn").addEventListener("click", () => {
  txModal.classList.remove("active");
});

document.getElementById("cancelTxModalBtn").addEventListener("click", () => {
  txModal.classList.remove("active");
});

window.openEditTxModal = function (tx) {
  document.getElementById("editTxId").value = tx.id;
  document.getElementById("txModalTitle").textContent = "Edit Transaction";
  document.getElementById("txAmount").value = tx.amount;
  document.getElementById("txDescription").value = tx.description || tx.text || "";
  document.getElementById("txDate").value = tx.date;

  const payment = tx.paymentMethod || "UPI";
  document.getElementById("txPaymentMethod").value = payment;
  if (payment === "UPI") {
    upiAppGroup.style.display = "block";
    document.getElementById("txUpiApp").value = tx.upiApp || "";
  } else {
    upiAppGroup.style.display = "none";
    document.getElementById("txUpiApp").value = "";
  }

  setTransactionType(tx.type || "EXPENSE", tx.category || "Groceries");
  txModal.classList.add("active");
};

document.getElementById("txForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const id = document.getElementById("editTxId").value;
  const amount = parseFloat(document.getElementById("txAmount").value);
  const description = document.getElementById("txDescription").value.trim();
  const date = document.getElementById("txDate").value;
  const paymentMethod = document.getElementById("txPaymentMethod").value;
  const upiApp = paymentMethod === "UPI" ? document.getElementById("txUpiApp").value.trim() : null;

  let category = txCategorySelect.value;
  if (category === "__CUSTOM__") {
    category = document.getElementById("txCustomCategory").value.trim();
    if (!category) {
      showToast("Please enter a custom category name.", "warning");
      return;
    }
  }

  if (isNaN(amount) || amount <= 0 || !description || !date) {
    showToast("Please provide valid transaction details.", "warning");
    return;
  }

  const payload = {
    amount,
    description,
    category,
    paymentMethod,
    upiApp,
    type: currentSelectedType,
    date,
  };

  const isEdit = Boolean(id);
  const url = isEdit
    ? `${API_BASE}/transactions/${username}/${id}`
    : `${API_BASE}/transactions/${username}`;
  const method = isEdit ? "PUT" : "POST";

  try {
    const res = await fetch(url, {
      method,
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });

    const result = await res.json();
    if (!res.ok) throw new Error(result.message || "Operation failed");

    showToast(isEdit ? "Transaction updated successfully!" : "Transaction recorded successfully!", "success");
    txModal.classList.remove("active");

    fetchDashboard();
    if (currentTab === "transactions") fetchTransactions();
    if (currentTab === "analytics") fetchAnalytics();
    if (currentTab === "budgets") fetchBudgets();

  } catch (err) {
    showToast(err.message, "error");
  }
});

// Delete Transaction
window.deleteTransaction = async function (id) {
  if (!confirm("Are you sure you want to delete this transaction record?")) return;

  try {
    const res = await fetch(`${API_BASE}/transactions/${username}/${id}`, {
      method: "DELETE",
    });

    if (!res.ok) {
      const err = await res.json();
      throw new Error(err.message || "Failed to delete");
    }

    showToast("Transaction deleted successfully", "success");
    fetchDashboard();
    if (currentTab === "transactions") fetchTransactions();
    if (currentTab === "analytics") fetchAnalytics();
    if (currentTab === "budgets") fetchBudgets();

  } catch (err) {
    showToast(err.message, "error");
  }
};

// --------------------------------------------------------------------------
// Analytics Tab (INR Formatting & Trends)
// --------------------------------------------------------------------------
async function fetchAnalytics() {
  try {
    const res = await fetch(`${API_BASE}/analytics/reports/${username}?months=6`);
    const result = await res.json();
    if (!res.ok) throw new Error(result.message || "Failed to load reports");
    const data = result.data;

    document.getElementById("topCategoryVal").textContent = data.highestSpendingCategory || "None";
    document.getElementById("avgMonthlySpendVal").textContent = formatCurrency(data.averageMonthlyExpense);
    document.getElementById("netLifetimeSavingsVal").textContent = formatCurrency(data.netSavings);

    renderTrendChart(data.monthlyTrends);

    const tbody = document.getElementById("categoryTableBody");
    tbody.innerHTML = "";
    if (!data.categoryBreakdown || data.categoryBreakdown.length === 0) {
      tbody.innerHTML = `<tr><td colspan="3" class="empty-state">No category expense records found.</td></tr>`;
      return;
    }

    data.categoryBreakdown.forEach((item) => {
      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td><strong>🏷️ ${escapeHtml(item.category)}</strong></td>
        <td><strong>${formatCurrency(item.amount)}</strong></td>
        <td>
          <div style="display: flex; align-items: center; gap: 10px;">
            <div class="budget-progress-track" style="flex: 1;">
              <div class="budget-progress-fill fill-normal" style="width: ${item.percentage}%;"></div>
            </div>
            <span style="font-size: 12px; font-weight: 600;">${item.percentage.toFixed(1)}%</span>
          </div>
        </td>
      `;
      tbody.appendChild(tr);
    });

  } catch (err) {
    showToast(err.message, "error");
  }
}

function renderTrendChart(trends) {
  const canvas = document.getElementById("monthlyTrendChart");
  if (!canvas) return;
  const ctx = canvas.getContext("2d");

  if (trendChartInstance) {
    trendChartInstance.destroy();
  }

  const labels = trends.map((t) => t.monthName);
  const incomeData = trends.map((t) => t.income);
  const expenseData = trends.map((t) => t.expense);

  trendChartInstance = new Chart(ctx, {
    type: "line",
    data: {
      labels: labels,
      datasets: [
        {
          label: "Income (₹)",
          data: incomeData,
          borderColor: "#10b981",
          backgroundColor: "rgba(16, 185, 129, 0.1)",
          tension: 0.35,
          fill: true,
          pointRadius: 4,
        },
        {
          label: "Expenses (₹)",
          data: expenseData,
          borderColor: "#ef4444",
          backgroundColor: "rgba(239, 68, 68, 0.1)",
          tension: 0.35,
          fill: true,
          pointRadius: 4,
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { position: "top" },
        tooltip: {
          callbacks: {
            label: (ctx) => ` ${ctx.dataset.label}: ${formatCurrency(ctx.raw)}`,
          },
        },
      },
      scales: {
        y: {
          beginAtZero: true,
          grid: { color: "#f1f5f9" },
          ticks: { callback: (v) => formatCurrency(v) },
        },
        x: { grid: { display: false } },
      },
    },
  });
}

// --------------------------------------------------------------------------
// Budgets Management Tab (INR Limits & Calculations)
// --------------------------------------------------------------------------
const budgetModal = document.getElementById("budgetModal");
const budgetCategorySelect = document.getElementById("budgetCategorySelect");
const budgetCustomCategoryGroup = document.getElementById("budgetCustomCategoryGroup");

budgetCategorySelect.addEventListener("change", (e) => {
  if (e.target.value === "__CUSTOM__") {
    budgetCustomCategoryGroup.style.display = "block";
    document.getElementById("budgetCustomCategory").focus();
  } else {
    budgetCustomCategoryGroup.style.display = "none";
  }
});

document.getElementById("openAddBudgetModalBtn").addEventListener("click", () => {
  document.getElementById("budgetForm").reset();
  document.getElementById("budgetMonth").value = new Date().getMonth() + 1;
  budgetCustomCategoryGroup.style.display = "none";
  budgetModal.classList.add("active");
});

document.getElementById("closeBudgetModalBtn").addEventListener("click", () => {
  budgetModal.classList.remove("active");
});

document.getElementById("cancelBudgetModalBtn").addEventListener("click", () => {
  budgetModal.classList.remove("active");
});

document.getElementById("budgetForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  let category = budgetCategorySelect.value;
  if (category === "__CUSTOM__") {
    category = document.getElementById("budgetCustomCategory").value.trim();
    if (!category) {
      showToast("Please enter a category name for the budget.", "warning");
      return;
    }
  }

  const monthlyLimit = parseFloat(document.getElementById("budgetLimit").value);
  const month = parseInt(document.getElementById("budgetMonth").value, 10);
  const year = new Date().getFullYear();

  if (isNaN(monthlyLimit) || monthlyLimit <= 0) {
    showToast("Please enter a valid monthly limit in ₹.", "warning");
    return;
  }

  try {
    const res = await fetch(`${API_BASE}/budgets/${username}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ category, monthlyLimit, month, year }),
    });

    const result = await res.json();
    if (!res.ok) throw new Error(result.message || "Failed to set budget");

    showToast("Budget configured successfully in ₹!", "success");
    budgetModal.classList.remove("active");
    fetchBudgets();
    fetchDashboard();

  } catch (err) {
    showToast(err.message, "error");
  }
});

async function fetchBudgets() {
  const currentMonth = new Date().getMonth() + 1;
  const currentYear = new Date().getFullYear();

  try {
    const res = await fetch(`${API_BASE}/budgets/${username}?month=${currentMonth}&year=${currentYear}`);
    const result = await res.json();
    if (!res.ok) throw new Error(result.message || "Failed to load budgets");
    const budgets = result.data;

    const grid = document.getElementById("budgetCardsGrid");
    grid.innerHTML = "";

    if (!budgets || budgets.length === 0) {
      grid.innerHTML = `
        <div class="dashboard-card" style="grid-column: 1 / -1; text-align: center; padding: 40px;">
          <div class="empty-icon">🎯</div>
          <h3>No Budgets Set</h3>
          <p style="color: var(--text-muted); margin: 8px 0 16px;">Set spending targets in ₹ for your categories to avoid overspending.</p>
          <button class="btn btn-primary btn-sm" onclick="document.getElementById('openAddBudgetModalBtn').click()">+ Create First Budget</button>
        </div>
      `;
      return;
    }

    budgets.forEach((b) => {
      const card = document.createElement("div");
      card.className = "budget-card";

      const fillClass = b.status === "EXCEEDED" ? "fill-exceeded" : (b.status === "WARNING" ? "fill-warning" : "fill-normal");
      const badgeClass = b.status === "EXCEEDED" ? "negative" : (b.status === "WARNING" ? "neutral" : "positive");
      const cappedPercent = Math.min(100, b.percentageUsed);

      card.innerHTML = `
        <div class="budget-header">
          <span class="budget-category-title">🎯 ${escapeHtml(b.category)}</span>
          <span class="kpi-badge ${badgeClass}">${b.status}</span>
        </div>
        <div class="budget-numbers">
          <span>Spent: <strong>${formatCurrency(b.spent)}</strong></span>
          <span>Limit: <strong>${formatCurrency(b.monthlyLimit)}</strong></span>
        </div>
        <div class="budget-progress-track">
          <div class="budget-progress-fill ${fillClass}" style="width: ${cappedPercent}%;"></div>
        </div>
        <div style="display: flex; justify-content: space-between; align-items: center; font-size: 12px; color: var(--text-muted);">
          <span>${b.percentageUsed.toFixed(1)}% utilized (${formatCurrency(b.remaining)} remaining)</span>
          <button class="btn-icon delete" title="Remove Budget" onclick="deleteBudget(${b.id})">🗑️</button>
        </div>
      `;
      grid.appendChild(card);
    });

  } catch (err) {
    showToast(err.message, "error");
  }
}

window.deleteBudget = async function (id) {
  if (!confirm("Are you sure you want to remove this budget limit?")) return;
  try {
    const res = await fetch(`${API_BASE}/budgets/${username}/${id}`, { method: "DELETE" });
    if (!res.ok) throw new Error("Failed to delete budget");
    showToast("Budget limit removed", "success");
    fetchBudgets();
    fetchDashboard();
  } catch (err) {
    showToast(err.message, "error");
  }
};

// --------------------------------------------------------------------------
// User Profile & Settings Tab
// --------------------------------------------------------------------------
async function fetchProfile() {
  try {
    const res = await fetch(`${API_BASE}/users/profile/${username}`);
    const result = await res.json();
    if (!res.ok) throw new Error(result.message || "Failed to load profile");
    const profile = result.data;

    document.getElementById("profileUsername").value = profile.username;
    document.getElementById("profileName").value = profile.name || profile.username;
    document.getElementById("profileEmail").value = profile.email || "";

    document.getElementById("sidebarUserName").textContent = profile.name || profile.username;
    document.getElementById("sidebarUserEmail").textContent = profile.email || "";
    document.getElementById("userAvatar").textContent = (profile.name || profile.username).charAt(0).toUpperCase();

  } catch (err) {
    showToast(err.message, "error");
  }
}

document.getElementById("updateProfileForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const name = document.getElementById("profileName").value.trim();
  const email = document.getElementById("profileEmail").value.trim();

  try {
    const res = await fetch(`${API_BASE}/users/profile/${username}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ name, email }),
    });

    const result = await res.json();
    if (!res.ok) throw new Error(result.message || "Profile update failed");

    showToast("Profile details updated successfully!", "success");
    fetchProfile();

  } catch (err) {
    showToast(err.message, "error");
  }
});

document.getElementById("changePasswordForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const currentPassword = document.getElementById("currentPassword").value;
  const newPassword = document.getElementById("newPassword").value;

  if (newPassword.length < 6) {
    showToast("New password must be at least 6 characters.", "warning");
    return;
  }

  try {
    const res = await fetch(`${API_BASE}/users/password/${username}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ currentPassword, newPassword }),
    });

    const result = await res.json();
    if (!res.ok) throw new Error(result.message || "Password change failed");

    showToast("Password updated successfully!", "success");
    document.getElementById("changePasswordForm").reset();

  } catch (err) {
    showToast(err.message, "error");
  }
});

// --------------------------------------------------------------------------
// CSV Export Functionality (INR)
// --------------------------------------------------------------------------
document.getElementById("exportCsvBtn").addEventListener("click", () => {
  const exportUrl = `${API_BASE}/export/csv/${username}`;
  window.open(exportUrl, "_blank");
  showToast("Downloading transactions CSV in INR format...", "info");
});

// --------------------------------------------------------------------------
// Utilities
// --------------------------------------------------------------------------
function debounce(func, wait) {
  let timeout;
  return function (...args) {
    clearTimeout(timeout);
    timeout = setTimeout(() => func.apply(this, args), wait);
  };
}

function escapeHtml(str) {
  if (!str) return "";
  return str
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}

// Initial Launch
fetchProfile();
fetchDashboard();
