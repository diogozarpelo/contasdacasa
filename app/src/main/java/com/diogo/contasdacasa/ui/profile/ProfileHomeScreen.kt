package com.diogo.contasdacasa.ui.profile

import com.diogo.contasdacasa.data.model.BillEntryType

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.diogo.contasdacasa.data.model.Bill
import com.diogo.contasdacasa.data.model.Profile
import com.diogo.contasdacasa.ui.util.formatCurrency
import com.diogo.contasdacasa.ui.util.formatMonthName

@Composable
fun ProfileHomeScreen(
    profile: Profile,
    bills: List<Bill>,
    month: Int,
    year: Int,
    isLoading: Boolean,
    errorMessage: String?,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onPrepareNextMonth: () -> Unit,
    onAddBill: () -> Unit,
    onEditBill: (Bill) -> Unit,
    onTogglePaid: (Bill) -> Unit,
    onDeleteBill: (Bill) -> Unit,
    onDeleteInstallmentsFromCurrent: (Bill) -> Unit,
    onChangeProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    var billPendingDeletion by remember {
        mutableStateOf<Bill?>(null)
    }

    var selectedFilter by remember(month, year) {
        mutableStateOf(BillFilter.ALL)
    }

    val monthName = formatMonthName(month)

    val totalInCents = bills.sumOf { bill ->
        bill.amountInCents
    }

    val paidInCents = bills
        .filter { bill -> bill.isPaid }
        .sumOf { bill -> bill.amountInCents }

    val pendingInCents = totalInCents - paidInCents
    val paidCount = bills.count { bill -> bill.isPaid }

    val filteredBills = when (selectedFilter) {
        BillFilter.ALL -> bills
        BillFilter.PENDING -> bills.filter { bill -> !bill.isPaid }
        BillFilter.PAID -> bills.filter { bill -> bill.isPaid }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 20.dp,
            end = 16.dp,
            bottom = 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HomeHeader(
                profileName = profile.name,
                monthName = monthName,
                year = year,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onChangeProfile = onChangeProfile
            )
        }

        item {
            FinancialSummary(
                totalInCents = totalInCents,
                paidInCents = paidInCents,
                pendingInCents = pendingInCents,
                paidCount = paidCount,
                totalCount = bills.size
            )
        }

        errorMessage?.let { message ->
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(14.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            AccountsSectionHeader(
                billCount = filteredBills.size,
                selectedFilter = selectedFilter,
                onFilterSelected = { filter ->
                    selectedFilter = filter
                }
            )
        }

        when {
            isLoading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            bills.isEmpty() -> {
                item {
                    EmptyAccountsCard(
                        onAddBill = onAddBill
                    )
                }
            }

            filteredBills.isEmpty() -> {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = if (selectedFilter == BillFilter.PAID) {
                                "Nenhuma conta paga neste mês."
                            } else {
                                "Nenhuma conta pendente neste mês."
                            },
                            modifier = Modifier.padding(20.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            else -> {
                items(
                    items = filteredBills,
                    key = { bill -> bill.id }
                ) { bill ->
                    BillCard(
                        bill = bill,
                        onTogglePaid = {
                            onTogglePaid(bill)
                        },
                        onEdit = {
                            onEditBill(bill)
                        },
                        onDelete = {
                            billPendingDeletion = bill
                        }
                    )
                }
            }
        }
        item {
            Button(
                onClick = onAddBill,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Adicionar lançamento",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        item {
            OutlinedButton(
                onClick = onPrepareNextMonth,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = bills.any { bill ->
                    bill.entryType == BillEntryType.MONTHLY
                },
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(text = "Preparar próximo mês")
            }
        }
    }

    billPendingDeletion?.let { bill ->
        DeleteBillDialog(
            bill = bill,
            onDeleteBill = {
                onDeleteBill(bill)
                billPendingDeletion = null
            },
            onDeleteInstallmentsFromCurrent = {
                onDeleteInstallmentsFromCurrent(bill)
                billPendingDeletion = null
            },
            onDismiss = {
                billPendingDeletion = null
            }
        )
    }
}
