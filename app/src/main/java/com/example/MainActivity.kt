package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddEditTransactionSheet
import com.example.ui.components.AnalyticsComponent
import com.example.ui.components.DashboardComponent
import com.example.ui.components.SheetViewComponent
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ExpenseTrackerViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ExpenseTrackerViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: ExpenseTrackerViewModel) {
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val monthlySummary by viewModel.monthlySummary.collectAsStateWithLifecycle()
    val filteredTransactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterType by viewModel.filterType.collectAsStateWithLifecycle()
    val showAddEditDialog by viewModel.showAddEditDialog.collectAsStateWithLifecycle()
    val editingTransaction by viewModel.editingTransaction.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "হিসাব খাতা (Hisab Khata)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { viewModel.setActiveTab(0) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "ড্যাশবোর্ড") },
                    label = { Text("ড্যাশবোর্ড") },
                    modifier = Modifier.testTag("nav_dashboard_tab")
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { viewModel.setActiveTab(1) },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "হিসাব শীট") },
                    label = { Text("হিসাব শীট") },
                    modifier = Modifier.testTag("nav_sheet_tab")
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { viewModel.setActiveTab(2) },
                    icon = { Icon(Icons.Default.Insights, contentDescription = "বিশ্লেষণ") },
                    label = { Text("বিশ্লেষণ") },
                    modifier = Modifier.testTag("nav_analytics_tab")
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openAddDialog() },
                icon = { Icon(Icons.Default.Add, contentDescription = "নতুন হিসাব যোগ") },
                text = { Text("নতুন হিসাব", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("main_add_fab")
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 640.dp)
            ) {
                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tab_transition"
                ) { tab ->
                    when (tab) {
                        0 -> DashboardComponent(
                            summary = monthlySummary,
                            onPreviousMonth = { viewModel.previousMonth() },
                            onNextMonth = { viewModel.nextMonth() },
                            onResetToCurrentMonth = { viewModel.resetToCurrentMonth() },
                            onAddTransactionClick = { viewModel.openAddDialog() },
                            onViewSheetClick = { viewModel.setActiveTab(1) },
                            onEditTransaction = { tx -> viewModel.openEditDialog(tx) },
                            onResetSampleData = { viewModel.resetWithSampleData() }
                        )
                        1 -> SheetViewComponent(
                            transactions = filteredTransactions,
                            selectedMonth = selectedMonth,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { viewModel.setSearchQuery(it) },
                            filterType = filterType,
                            onFilterTypeChange = { viewModel.setFilterType(it) },
                            onEditTransaction = { tx -> viewModel.openEditDialog(tx) },
                            onDeleteTransaction = { tx -> viewModel.deleteTransaction(tx) }
                        )
                        2 -> AnalyticsComponent(
                            summary = monthlySummary
                        )
                    }
                }
            }
        }

        // Add / Edit Transaction Bottom Sheet
        if (showAddEditDialog) {
            AddEditTransactionSheet(
                transactionToEdit = editingTransaction,
                onDismiss = { viewModel.closeAddEditDialog() },
                onSave = { title, amount, type, category, paymentMethod, timestamp, note ->
                    viewModel.saveTransaction(title, amount, type, category, paymentMethod, timestamp, note)
                }
            )
        }
    }
}
