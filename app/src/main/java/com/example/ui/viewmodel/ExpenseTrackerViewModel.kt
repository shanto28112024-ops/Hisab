package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

data class MonthlySummary(
    val selectedMonth: YearMonth = YearMonth.now(),
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val remainingBalance: Double = 0.0,
    val todayExpense: Double = 0.0,
    val savingsPercentage: Double = 0.0,
    val expensePercentage: Double = 0.0,
    val monthlyTransactions: List<TransactionEntity> = emptyList(),
    val incomeCategories: Map<String, Double> = emptyMap(),
    val expenseCategories: Map<String, Double> = emptyMap(),
    val topExpenseCategory: Pair<String, Double>? = null,
    val topIncomeSource: Pair<String, Double>? = null
)

class ExpenseTrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TransactionRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TransactionRepository(database.transactionDao())
        viewModelScope.launch {
            repository.seedSampleDataIfEmpty()
        }
    }

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterType = MutableStateFlow<TransactionType?>(null)
    val filterType: StateFlow<TransactionType?> = _filterType.asStateFlow()

    private val _filterCategory = MutableStateFlow<String?>(null)
    val filterCategory: StateFlow<String?> = _filterCategory.asStateFlow()

    private val _activeTab = MutableStateFlow(0) // 0: Dashboard, 1: Sheet, 2: Analytics
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    private val _showAddEditDialog = MutableStateFlow(false)
    val showAddEditDialog: StateFlow<Boolean> = _showAddEditDialog.asStateFlow()

    private val _editingTransaction = MutableStateFlow<TransactionEntity?>(null)
    val editingTransaction: StateFlow<TransactionEntity?> = _editingTransaction.asStateFlow()

    // Monthly summary calculation combining all transactions and selected month
    val monthlySummary: StateFlow<MonthlySummary> = combine(
        allTransactions,
        _selectedMonth
    ) { transactions, month ->
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now()

        val monthlyItems = transactions.filter { tx ->
            val txDate = Instant.ofEpochMilli(tx.timestamp).atZone(zone).toLocalDate()
            YearMonth.from(txDate) == month
        }

        var incomeTotal = 0.0
        var expenseTotal = 0.0
        var todayExp = 0.0
        val incomeCatMap = mutableMapOf<String, Double>()
        val expenseCatMap = mutableMapOf<String, Double>()

        for (tx in monthlyItems) {
            val txDate = Instant.ofEpochMilli(tx.timestamp).atZone(zone).toLocalDate()
            if (tx.type == TransactionType.INCOME) {
                incomeTotal += tx.amount
                incomeCatMap[tx.category] = (incomeCatMap[tx.category] ?: 0.0) + tx.amount
            } else {
                expenseTotal += tx.amount
                expenseCatMap[tx.category] = (expenseCatMap[tx.category] ?: 0.0) + tx.amount
                if (txDate == today) {
                    todayExp += tx.amount
                }
            }
        }

        val balance = incomeTotal - expenseTotal
        val savingsPct = if (incomeTotal > 0) ((balance / incomeTotal) * 100.0).coerceAtLeast(0.0) else 0.0
        val expensePct = if (incomeTotal > 0) ((expenseTotal / incomeTotal) * 100.0) else 0.0

        val topExpense = expenseCatMap.maxByOrNull { it.value }?.toPair()
        val topIncome = incomeCatMap.maxByOrNull { it.value }?.toPair()

        MonthlySummary(
            selectedMonth = month,
            totalIncome = incomeTotal,
            totalExpenses = expenseTotal,
            remainingBalance = balance,
            todayExpense = todayExp,
            savingsPercentage = savingsPct,
            expensePercentage = expensePct,
            monthlyTransactions = monthlyItems,
            incomeCategories = incomeCatMap.toList().sortedByDescending { it.second }.toMap(),
            expenseCategories = expenseCatMap.toList().sortedByDescending { it.second }.toMap(),
            topExpenseCategory = topExpense,
            topIncomeSource = topIncome
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MonthlySummary()
    )

    // Filtered transactions for the Sheet View
    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        allTransactions,
        _selectedMonth,
        _searchQuery,
        _filterType,
        _filterCategory
    ) { transactions, month, query, typeFilter, catFilter ->
        val zone = ZoneId.systemDefault()
        transactions.filter { tx ->
            val txDate = Instant.ofEpochMilli(tx.timestamp).atZone(zone).toLocalDate()
            val matchesMonth = YearMonth.from(txDate) == month
            val matchesType = typeFilter == null || tx.type == typeFilter
            val matchesCategory = catFilter == null || tx.category == catFilter
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                tx.title.contains(query, ignoreCase = true) ||
                tx.category.contains(query, ignoreCase = true) ||
                tx.note.contains(query, ignoreCase = true) ||
                tx.paymentMethod.contains(query, ignoreCase = true) ||
                tx.amount.toString().contains(query)
            }
            matchesMonth && matchesType && matchesCategory && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setMonth(yearMonth: YearMonth) {
        _selectedMonth.value = yearMonth
    }

    fun previousMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
    }

    fun resetToCurrentMonth() {
        _selectedMonth.value = YearMonth.now()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterType(type: TransactionType?) {
        _filterType.value = type
    }

    fun setFilterCategory(category: String?) {
        _filterCategory.value = category
    }

    fun setActiveTab(tabIndex: Int) {
        _activeTab.value = tabIndex
    }

    fun openAddDialog() {
        _editingTransaction.value = null
        _showAddEditDialog.value = true
    }

    fun openEditDialog(transaction: TransactionEntity) {
        _editingTransaction.value = transaction
        _showAddEditDialog.value = true
    }

    fun closeAddEditDialog() {
        _showAddEditDialog.value = false
        _editingTransaction.value = null
    }

    fun saveTransaction(
        title: String,
        amount: Double,
        type: TransactionType,
        category: String,
        paymentMethod: String,
        timestamp: Long,
        note: String
    ) {
        viewModelScope.launch {
            val currentEdit = _editingTransaction.value
            if (currentEdit != null) {
                repository.update(
                    currentEdit.copy(
                        title = title.trim(),
                        amount = amount,
                        type = type,
                        category = category.trim(),
                        paymentMethod = paymentMethod.trim(),
                        timestamp = timestamp,
                        note = note.trim()
                    )
                )
            } else {
                repository.insert(
                    TransactionEntity(
                        title = title.trim(),
                        amount = amount,
                        type = type,
                        category = category.trim(),
                        paymentMethod = paymentMethod.trim(),
                        timestamp = timestamp,
                        note = note.trim()
                    )
                )
            }
            closeAddEditDialog()
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.delete(transaction)
        }
    }

    fun resetWithSampleData() {
        viewModelScope.launch {
            repository.clearAll()
            repository.seedCurrentMonthData()
        }
    }
}
