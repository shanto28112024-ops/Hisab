package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

enum class TransactionType(val labelBn: String, val labelEn: String) {
    INCOME("জমা / আয়", "Income"),
    EXPENSE("খরচ / ব্যয়", "Expense")
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val paymentMethod: String,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
) {
    fun toLocalDate(): LocalDate {
        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }
}
