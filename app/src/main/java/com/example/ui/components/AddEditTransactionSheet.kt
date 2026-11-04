package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedContainer
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenContainer
import com.example.ui.util.FormatUtils
import java.time.LocalDate
import java.time.ZoneId

private val INCOME_CATEGORIES = listOf(
    "বেতন (Salary)",
    "ব্যবসা (Business)",
    "ফ্রিল্যান্সিং (Freelance)",
    "বিনিয়োগ লাভ (Investment)",
    "ভাড়া প্রাপ্তি (Rent Income)",
    "উপহার বা বোনাস (Bonus)",
    "অন্যান্য আয় (Other)"
)

private val EXPENSE_CATEGORIES = listOf(
    "খাবার ও বাজার (Groceries)",
    "বাসা ভাড়া (Rent)",
    "ইউটিলিটি ও বিল (Bills)",
    "যাতায়াত (Transport)",
    "চিকিৎসা (Health)",
    "কেনাকাটা (Shopping)",
    "শিক্ষা (Education)",
    "বিনোদন (Entertainment)",
    "পরিবার ও উপহার (Family)",
    "অন্যান্য ব্যয় (Other)"
)

private val PAYMENT_METHODS = listOf(
    "নগদ (Cash)",
    "বিকাশ (bKash)",
    "নগদ (Nagad)",
    "ব্যাংক (Bank)",
    "কার্ড (Card)"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionSheet(
    transactionToEdit: TransactionEntity?,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        amount: Double,
        type: TransactionType,
        category: String,
        paymentMethod: String,
        timestamp: Long,
        note: String
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var type by remember { mutableStateOf(transactionToEdit?.type ?: TransactionType.EXPENSE) }
    var title by remember { mutableStateOf(transactionToEdit?.title ?: "") }
    var amountText by remember { mutableStateOf(transactionToEdit?.amount?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() } ?: "") }
    var category by remember {
        mutableStateOf(
            transactionToEdit?.category ?: (if (type == TransactionType.INCOME) INCOME_CATEGORIES[0] else EXPENSE_CATEGORIES[0])
        )
    }
    var paymentMethod by remember { mutableStateOf(transactionToEdit?.paymentMethod ?: PAYMENT_METHODS[0]) }
    var timestamp by remember { mutableLongStateOf(transactionToEdit?.timestamp ?: System.currentTimeMillis()) }
    var note by remember { mutableStateOf(transactionToEdit?.note ?: "") }

    var titleError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    LaunchedEffect(type) {
        if (transactionToEdit == null) {
            category = if (type == TransactionType.INCOME) INCOME_CATEGORIES[0] else EXPENSE_CATEGORIES[0]
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("add_edit_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (transactionToEdit == null) "নতুন জমা / খরচ যোগ করুন" else "হিসাব সংশোধন করুন",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Income / Expense Segmented Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Income Button
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (type == TransactionType.INCOME) IncomeGreen else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { type = TransactionType.INCOME }
                        .testTag("type_income_selector")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = if (type == TransactionType.INCOME) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "জমা / আয় (Income)",
                            fontWeight = FontWeight.Bold,
                            color = if (type == TransactionType.INCOME) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }

                // Expense Button
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (type == TransactionType.EXPENSE) ExpenseRed else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { type = TransactionType.EXPENSE }
                        .testTag("type_expense_selector")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = if (type == TransactionType.EXPENSE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "খরচ / ব্যয় (Expense)",
                            fontWeight = FontWeight.Bold,
                            color = if (type == TransactionType.EXPENSE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Input with currency prefix
            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = it.filter { char -> char.isDigit() || char == '.' }
                    amountError = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("amount_input"),
                label = { Text("টাকার পরিমাণ (Amount)") },
                prefix = {
                    Text(
                        text = "৳ ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (type == TransactionType.INCOME) IncomeGreen else ExpenseRed
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = amountError,
                supportingText = {
                    if (amountError) Text("সঠিক পরিমাণ উল্লেখ করুন (Amount must be > 0)")
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Quick Amount Add Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(100, 500, 1000, 2000, 5000, 10000).forEach { chipAmount ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.clickable {
                            val cur = amountText.toDoubleOrNull() ?: 0.0
                            val updated = cur + chipAmount
                            amountText = if (updated % 1.0 == 0.0) updated.toLong().toString() else updated.toString()
                        }
                    ) {
                        Text(
                            text = "+৳$chipAmount",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title / Source Input
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    titleError = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("title_input"),
                label = { Text(if (type == TransactionType.INCOME) "আয়ের উৎস বা শিরোনাম (Source / Title)" else "ব্যয়ের কারণ বা পণ্য (Reason / Item)") },
                placeholder = { Text(if (type == TransactionType.INCOME) "যেমন: অফিস বেতন, প্রজেক্ট বোনাস" else "যেমন: মাসিক কাঁচাবাজার, বাসা ভাড়া") },
                isError = titleError,
                supportingText = {
                    if (titleError) Text("শিরোনাম আবশ্যক")
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Category Selection
            Text(
                text = "ক্যাটাগরি / খাত নির্বাচন করুন:",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            val currentCategories = if (type == TransactionType.INCOME) INCOME_CATEGORIES else EXPENSE_CATEGORIES
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                currentCategories.forEach { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (type == TransactionType.INCOME) IncomeGreenContainer else ExpenseRedContainer,
                            selectedLabelColor = if (type == TransactionType.INCOME) IncomeGreen else ExpenseRed
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Payment Method
            Text(
                text = "পরিশোধ বা প্রাপ্তির মাধ্যম:",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PAYMENT_METHODS.forEach { method ->
                    FilterChip(
                        selected = paymentMethod == method,
                        onClick = { paymentMethod = method },
                        label = { Text(method, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Note / Details
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_input"),
                label = { Text("অতিরিক্ত নোট বা বিস্তারিত (ঐচ্ছিক)") },
                placeholder = { Text("কোনো বিশেষ বিবরণ থাকলে লিখুন") },
                maxLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Save / Submit Button
            Button(
                onClick = {
                    val trimmedTitle = title.trim()
                    val parsedAmount = amountText.toDoubleOrNull() ?: 0.0

                    var hasError = false
                    if (trimmedTitle.isEmpty()) {
                        titleError = true
                        hasError = true
                    }
                    if (parsedAmount <= 0) {
                        amountError = true
                        hasError = true
                    }

                    if (!hasError) {
                        onSave(
                            trimmedTitle,
                            parsedAmount,
                            type,
                            category,
                            paymentMethod,
                            timestamp,
                            note.trim()
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_transaction_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (type == TransactionType.INCOME) IncomeGreen else ExpenseRed
                )
            ) {
                Text(
                    text = if (transactionToEdit == null) "সংরক্ষণ করুন (Save Entry)" else "আপডেট করুন (Update Entry)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
