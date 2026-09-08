package com.diogo.contasdacasa.ui.profile

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

    var showBills by remember(month, year) {
        mutableStateOf(true)
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
                billCount = bills.size,
                isExpanded = showBills,
                onToggle = {
                    showBills = !showBills
                }
            )
        }

        if (showBills) {
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

                else -> {
                    items(
                        items = bills,
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
                    bill.entryType == "MONTHLY"
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

@Composable
private fun HomeHeader(
    profileName: String,
    monthName: String,
    year: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onChangeProfile: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Olá, $profileName",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Confira as contas do mês",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TextButton(onClick = onChangeProfile) {
                Text(text = "Trocar perfil")
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MonthNavigationButton(
                    text = "‹",
                    description = "Mês anterior",
                    onClick = onPreviousMonth
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = monthName,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = year.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                MonthNavigationButton(
                    text = "›",
                    description = "Próximo mês",
                    onClick = onNextMonth
                )
            }
        }
    }
}

@Composable
private fun MonthNavigationButton(
    text: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(
                onClickLabel = description,
                onClick = onClick
            ),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = CircleShape
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun FinancialSummary(
    totalInCents: Long,
    paidInCents: Long,
    pendingInCents: Long,
    paidCount: Int,
    totalCount: Int
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(22.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Total do mês",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatCurrency(totalInCents),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "$paidCount de $totalCount contas pagas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryValueCard(
                label = "Pago",
                value = formatCurrency(paidInCents),
                indicatorColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )

            SummaryValueCard(
                label = "Pendente",
                value = formatCurrency(pendingInCents),
                indicatorColor = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryValueCard(
    label: String,
    value: String,
    indicatorColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(indicatorColor)
                )

                Spacer(modifier = Modifier.size(7.dp))

                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun AccountsSectionHeader(
    billCount: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onToggle),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 14.dp
            ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Contas",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.size(8.dp))

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                ) {
                    Text(
                        text = billCount.toString(),
                        modifier = Modifier.padding(
                            horizontal = 9.dp,
                            vertical = 3.dp
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Text(
                text = if (isExpanded) {
                    "Ocultar  ↑"
                } else {
                    "Ver contas  ↓"
                },
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun BillCard(
    bill: Bill,
    onTogglePaid: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val cardColor = if (bill.isPaid) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (bill.isPaid) 0.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = bill.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = if (bill.amountInCents == 0L) {
                            "Valor não informado"
                        } else {
                            formatCurrency(bill.amountInCents)
                        },
                        style = MaterialTheme.typography.titleLarge,
                        color = if (bill.amountInCents == 0L) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }

                PaymentStatusBadge(
                    isPaid = bill.isPaid
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (bill.dueDay == 0) {
                    "Vencimento não informado"
                } else {
                    "Vencimento: dia ${bill.dueDay}"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (bill.dueDay == 0) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            if (
                bill.entryType == "INSTALLMENT" &&
                bill.installmentNumber != null &&
                bill.totalInstallments != null
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Parcela ${bill.installmentNumber} de ${bill.totalInstallments}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onTogglePaid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                colors = if (bill.isPaid) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    ButtonDefaults.buttonColors()
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (bill.isPaid) {
                        "Marcar como pendente"
                    } else {
                        "Marcar como paga"
                    }
                )
            }

            Spacer(modifier = Modifier.height(7.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Editar")
                }

                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Excluir",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentStatusBadge(
    isPaid: Boolean
) {
    Surface(
        color = if (isPaid) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.errorContainer
        },
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = if (isPaid) {
                "Paga"
            } else {
                "Pendente"
            },
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 5.dp
            ),
            color = if (isPaid) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onErrorContainer
            },
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun EmptyAccountsCard(
    onAddBill: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Nenhuma conta neste mês",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Adicione o primeiro lançamento para começar.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onAddBill,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Adicionar conta")
            }
        }
    }
}

@Composable
private fun DeleteBillDialog(
    bill: Bill,
    onDeleteBill: () -> Unit,
    onDeleteInstallmentsFromCurrent: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Excluir lançamento?")
        },
        text = {
            Text(
                text = if (bill.entryType == "INSTALLMENT") {
                    "Escolha se deseja excluir somente esta parcela ou também as seguintes."
                } else {
                    "A conta ${bill.name} será excluída deste mês."
                }
            )
        },
        confirmButton = {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                TextButton(
                    onClick = onDeleteBill
                ) {
                    Text(
                        text = if (bill.entryType == "INSTALLMENT") {
                            "Excluir somente esta parcela"
                        } else {
                            "Excluir"
                        },
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (bill.entryType == "INSTALLMENT") {
                    TextButton(
                        onClick = onDeleteInstallmentsFromCurrent
                    ) {
                        Text(
                            text = "Excluir esta e as próximas",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(text = "Cancelar")
            }
        }
    )
}
