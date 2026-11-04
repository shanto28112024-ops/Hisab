package com.example

import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId

class ExpenseTrackerCalculationTest {

    @Test
    fun testMonthlyIncomeExpenseAndBalanceCalculation() {
        val today = LocalDate.of(2026, 10, 15)
        val zone = ZoneId.systemDefault()

        fun makeTimestamp(day: Int): Long {
            return today.withDayOfMonth(day)
                .atTime(LocalTime.of(12, 0))
                .atZone(zone)
                .toInstant()
                .toEpochMilli()
        }

        val testTransactions = listOf(
            TransactionEntity(
                id = 1,
                title = "বেতন",
                amount = 50000.0,
                type = TransactionType.INCOME,
                category = "বেতন (Salary)",
                paymentMethod = "Bank",
                timestamp = makeTimestamp(1)
            ),
            TransactionEntity(
                id = 2,
                title = "ফ্রিল্যান্স কাজ",
                amount = 20000.0,
                type = TransactionType.INCOME,
                category = "ফ্রিল্যান্সিং (Freelance)",
                paymentMethod = "bKash",
                timestamp = makeTimestamp(5)
            ),
            TransactionEntity(
                id = 3,
                title = "বাসা ভাড়া",
                amount = 18000.0,
                type = TransactionType.EXPENSE,
                category = "বাসা ভাড়া (Rent)",
                paymentMethod = "Bank",
                timestamp = makeTimestamp(2)
            ),
            TransactionEntity(
                id = 4,
                title = "বাজার খরচ",
                amount = 6000.0,
                type = TransactionType.EXPENSE,
                category = "খাবার ও বাজার (Groceries)",
                paymentMethod = "Cash",
                timestamp = makeTimestamp(10)
            ),
            TransactionEntity(
                id = 5,
                title = "বিদ্যুৎ বিল",
                amount = 2500.0,
                type = TransactionType.EXPENSE,
                category = "ইউটিলিটি ও বিল (Bills)",
                paymentMethod = "bKash",
                timestamp = makeTimestamp(12)
            )
        )

        val totalIncome = testTransactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }

        val totalExpense = testTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }

        val remainingBalance = totalIncome - totalExpense

        // Verify Total Income = 50,000 + 20,000 = 70,000
        assertEquals(70000.0, totalIncome, 0.001)

        // Verify Total Expense = 18,000 + 6,000 + 2,500 = 26,500
        assertEquals(26500.0, totalExpense, 0.001)

        // Verify Remaining Balance = 70,000 - 26,500 = 43,500
        assertEquals(43500.0, remainingBalance, 0.001)

        // Verify Savings Rate
        val savingsPercentage = (remainingBalance / totalIncome) * 100.0
        assertEquals(62.14, savingsPercentage, 0.01)

        // Verify category breakdown
        val expenseCategories = testTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        assertEquals(18000.0, expenseCategories["বাসা ভাড়া (Rent)"] ?: 0.0, 0.001)
        assertEquals(6000.0, expenseCategories["খাবার ও বাজার (Groceries)"] ?: 0.0, 0.001)
        assertEquals(2500.0, expenseCategories["ইউটিলিটি ও বিল (Bills)"] ?: 0.0, 0.001)
    }

    @Test
    fun testDeficitBalanceHandling() {
        val testTransactions = listOf(
            TransactionEntity(
                id = 1,
                title = "অল্প আয়",
                amount = 10000.0,
                type = TransactionType.INCOME,
                category = "বেতন",
                paymentMethod = "Cash",
                timestamp = System.currentTimeMillis()
            ),
            TransactionEntity(
                id = 2,
                title = "বড় জরুরি চিকিৎসা",
                amount = 25000.0,
                type = TransactionType.EXPENSE,
                category = "চিকিৎসা",
                paymentMethod = "Bank",
                timestamp = System.currentTimeMillis()
            )
        )

        val totalIncome = testTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense = testTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val remainingBalance = totalIncome - totalExpense

        assertEquals(10000.0, totalIncome, 0.001)
        assertEquals(25000.0, totalExpense, 0.001)
        assertEquals(-15000.0, remainingBalance, 0.001)
        assertTrue("Remaining balance is negative in deficit", remainingBalance < 0)
    }
}
