package com.example.data.repository

import com.example.data.local.TransactionDao
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class TransactionRepository(private val transactionDao: TransactionDao) {

    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    fun getTransactionsBetween(startMillis: Long, endMillis: Long): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsBetween(startMillis, endMillis)
    }

    suspend fun insert(transaction: TransactionEntity): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun update(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun delete(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun deleteById(id: Long) {
        transactionDao.deleteById(id)
    }

    suspend fun clearAll() {
        transactionDao.clearAll()
    }

    suspend fun seedSampleDataIfEmpty() {
        val count = transactionDao.getCount()
        if (count == 0) {
            seedCurrentMonthData()
        }
    }

    suspend fun seedCurrentMonthData() {
        val today = LocalDate.now()
        val zone = ZoneId.systemDefault()

        fun dateMillis(dayOfMonth: Int, hour: Int = 12): Long {
            val clampedDay = dayOfMonth.coerceIn(1, today.lengthOfMonth())
            val date = today.withDayOfMonth(clampedDay).atTime(LocalTime.of(hour, 0))
            return date.atZone(zone).toInstant().toEpochMilli()
        }

        val sampleTransactions = listOf(
            TransactionEntity(
                title = "অফিস মাসিক বেতন",
                amount = 65000.0,
                type = TransactionType.INCOME,
                category = "বেতন (Salary)",
                paymentMethod = "ব্যাংক (Bank)",
                timestamp = dateMillis(1, 10),
                note = "চলতি মাসের মূল বেতন সরাসরি ব্যাংকে জমা হয়েছে।"
            ),
            TransactionEntity(
                title = "ফ্রিল্যান্সিং মোবাইল অ্যাপ ডিজাইন",
                amount = 18500.0,
                type = TransactionType.INCOME,
                category = "ফ্রিল্যান্সিং (Freelance)",
                paymentMethod = "বিকাশ (bKash)",
                timestamp = dateMillis(5, 16),
                note = "ইউআই ডিজাইন ও প্রোটোটাইপিং পেমেন্ট"
            ),
            TransactionEntity(
                title = "বাসা ভাড়া প্রদান",
                amount = 22000.0,
                type = TransactionType.EXPENSE,
                category = "বাসা ভাড়া (Rent)",
                paymentMethod = "ব্যাংক (Bank)",
                timestamp = dateMillis(2, 11),
                note = "ফ্ল্যাটের মাসিক ভাড়া ও সার্ভিস চার্জ"
            ),
            TransactionEntity(
                title = "সুপারশপ ও কাঁচাবাজার",
                amount = 8400.0,
                type = TransactionType.EXPENSE,
                category = "খাবার ও বাজার (Groceries)",
                paymentMethod = "নগদ (Cash)",
                timestamp = dateMillis(4, 9),
                note = "চাল, ডাল, তেল, মাছ এবং শাকসবজি কেনা"
            ),
            TransactionEntity(
                title = "বিদ্যুৎ ও গ্যাস বিল",
                amount = 3250.0,
                type = TransactionType.EXPENSE,
                category = "ইউটিলিটি ও বিল (Bills)",
                paymentMethod = "বিকাশ (bKash)",
                timestamp = dateMillis(6, 14),
                note = "ডেসকো প্রিপেইড মিটার রিচার্জ ও গ্যাস বিল"
            ),
            TransactionEntity(
                title = "ইন্টারনেট ও ওয়াইফাই বিল",
                amount = 1200.0,
                type = TransactionType.EXPENSE,
                category = "ইউটিলিটি ও বিল (Bills)",
                paymentMethod = "বিকাশ (bKash)",
                timestamp = dateMillis(7, 15),
                note = "মাসিক অপটিক্যাল ফাইবার ব্রডব্যান্ড বিল"
            ),
            TransactionEntity(
                title = "সাপ্তাহিক যাতায়াত ও জ্বালানি",
                amount = 2800.0,
                type = TransactionType.EXPENSE,
                category = "যাতায়াত (Transport)",
                paymentMethod = "নগদ (Cash)",
                timestamp = dateMillis(9, 18),
                note = "বাইকের পেট্রোল ও উবার রাইড খরচ"
            ),
            TransactionEntity(
                title = "ওষুধ ও ডাক্তারের পরামর্শ",
                amount = 1950.0,
                type = TransactionType.EXPENSE,
                category = "চিকিৎসা (Health)",
                paymentMethod = "কার্ড (Card)",
                timestamp = dateMillis(11, 20),
                note = "প্রেসক্রিপশনের নিয়মিত ওষুধ ও ভিটামিন"
            ),
            TransactionEntity(
                title = "রেস্তোরাঁয় ডিনার ও স্ন্যাক্স",
                amount = 1750.0,
                type = TransactionType.EXPENSE,
                category = "খাবার ও বাজার (Groceries)",
                paymentMethod = "কার্ড (Card)",
                timestamp = dateMillis(13, 21),
                note = "বন্ধুদের সাথে উইকেন্ড গেট-টুগেদার"
            )
        )

        transactionDao.insertAll(sampleTransactions)
    }
}
