package com.example.ui.util

import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object FormatUtils {

    private val numberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 0
    }

    fun formatCurrency(amount: Double): String {
        return "৳ " + numberFormat.format(amount)
    }

    fun formatMonthYear(yearMonth: YearMonth): String {
        val monthBn = when (yearMonth.monthValue) {
            1 -> "জানুয়ারি"
            2 -> "ফেব্রুয়ারি"
            3 -> "মার্চ"
            4 -> "এপ্রিল"
            5 -> "মে"
            6 -> "জুন"
            7 -> "জুলাই"
            8 -> "আগস্ট"
            9 -> "সেপ্টেম্বর"
            10 -> "অক্টোবর"
            11 -> "নভেম্বর"
            12 -> "ডিসেম্বর"
            else -> yearMonth.month.name
        }
        val monthEn = yearMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }
        return "$monthBn ($monthEn) ${yearMonth.year}"
    }

    fun formatDate(timestamp: Long): String {
        val date = Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return date.format(DateTimeFormatter.ofPattern("dd MMM, yyyy", Locale.ENGLISH))
    }

    fun formatTime(timestamp: Long): String {
        val time = Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
        return time.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH))
    }

    fun formatFullDateTime(timestamp: Long): String {
        return "${formatDate(timestamp)} • ${formatTime(timestamp)}"
    }
}
