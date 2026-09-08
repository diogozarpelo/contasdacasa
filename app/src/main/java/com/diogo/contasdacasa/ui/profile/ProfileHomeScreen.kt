package com.diogo.contasdacasa.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.diogo.contasdacasa.data.model.Bill
import com.diogo.contasdacasa.data.model.Profile
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

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
    onAddBill: () -> Unit,
    onEditBill: (Bill) -> Unit,
    onTogglePaid: (Bill) -> Unit,
    onDeleteBill: (Bill) -> Unit,
    onChangeProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    var billPendingDeletion by remember {
        mutableStateOf<Bill?>(null)
    }

    val monthName = Month
        .of(month)
        .getDisplayName(TextStyle.FULL, Locale("pt", "BR"))
        .replaceFirstChar { character ->
            character.uppercase()
        }

    val totalInCents = bills.sumOf { bill ->
        bill.amountInCents
    }

    val paidInCents = bills
        .filter { bill -> bill.isPaid }
        .sumOf { bill -> bill.amountInCents }

    val pendingInCents = totalInCents - paidInCents

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Olá, ${profile.name}",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = onPreviousMonth) {
                Text(text = "<")
            }

            Text(
                text = "$monthName de $year",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedButton(onClick = onNextMonth) {
                Text(text = ">")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "Total: ${formatCurrency(totalInCents)}")
                Text(text = "Pago: ${formatCurrency(paidInCents)}")
                Text(text = "Pendente: ${formatCurrency(pendingInCents)}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        when {
            isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }

            bills.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Nenhuma conta cadastrada neste mês.")
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = bills,
                        key = { bill -> bill.id }
                    ) { bill ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = bill.name,
                                            style = MaterialTheme.typography.titleMedium
                                        )

                                        Text(
                                            text = if (bill.amountInCents == 0L) {
                                                "Valor não informado"
                                            } else {
                                                formatCurrency(bill.amountInCents)
                                            },
                                            color = if (bill.amountInCents == 0L) {
                                                MaterialTheme.colorScheme.error
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            }
                                        )

                                        Text(
                                            text = if (bill.dueDay == 0) {
                                                "Vencimento não informado"
                                            } else {
                                                "Vence no dia ${bill.dueDay}"
                                            },
                                            color = if (bill.dueDay == 0) {
                                                MaterialTheme.colorScheme.error
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            }
                                        )

                                        if (
                                            bill.entryType == "INSTALLMENT" &&
                                            bill.installmentNumber != null &&
                                            bill.totalInstallments != null
                                        ) {
                                            Text(
                                                text = "Parcela ${bill.installmentNumber} de ${bill.totalInstallments}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    Checkbox(
                                        checked = bill.isPaid,
                                        onCheckedChange = {
                                            onTogglePaid(bill)
                                        }
                                    )
                                }

                                Row {
                                    TextButton(
                                        onClick = {
                                            onEditBill(bill)
                                        }
                                    ) {
                                        Text(text = "Editar")
                                    }

                                    TextButton(
                                        onClick = {
                                            billPendingDeletion = bill
                                        }
                                    ) {
                                        Text(text = "Excluir")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onAddBill,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Adicionar conta")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onChangeProfile,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Trocar de perfil")
        }
    }

    billPendingDeletion?.let { bill ->
        AlertDialog(
            onDismissRequest = {
                billPendingDeletion = null
            },
            title = {
                Text(text = "Excluir lançamento?")
            },
            text = {
                Text(
                    text = if (bill.entryType == "INSTALLMENT") {
                        "A parcela ${bill.installmentNumber} de ${bill.totalInstallments} será excluída somente deste mês."
                    } else {
                        "A conta ${bill.name} será excluída deste mês."
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteBill(bill)
                        billPendingDeletion = null
                    }
                ) {
                    Text(text = "Excluir")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        billPendingDeletion = null
                    }
                ) {
                    Text(text = "Cancelar")
                }
            }
        )
    }
}
private fun formatCurrency(amountInCents: Long): String {
    val amount = BigDecimal(amountInCents).movePointLeft(2)

    return NumberFormat
        .getCurrencyInstance(Locale("pt", "BR"))
        .format(amount)
}