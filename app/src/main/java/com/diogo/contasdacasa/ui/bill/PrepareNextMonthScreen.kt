package com.diogo.contasdacasa.ui.bill

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.diogo.contasdacasa.data.model.Bill
import com.diogo.contasdacasa.ui.util.formatMonthName
import java.time.YearMonth

@Composable
fun PrepareNextMonthScreen(
    bills: List<Bill>,
    sourceMonth: Int,
    sourceYear: Int,
    isSaving: Boolean,
    errorMessage: String?,
    onConfirm: (List<Bill>, Set<Long>) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val monthlyBills = bills.filter { bill ->
        bill.entryType == "MONTHLY"
    }

    var selectedIds by remember(
        sourceMonth,
        sourceYear,
        monthlyBills
    ) {
        mutableStateOf<Set<Long>>(emptySet())
    }

    var copyDetailsIds by remember(
        sourceMonth,
        sourceYear,
        monthlyBills
    ) {
        mutableStateOf<Set<Long>>(emptySet())
    }

    val targetMonth = YearMonth
        .of(sourceYear, sourceMonth)
        .plusMonths(1)

    val targetMonthName = formatMonthName(targetMonth.monthValue)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Preparar próximo mês",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "$targetMonthName de ${targetMonth.year}",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Escolha quais contas mensais serão levadas.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (monthlyBills.isNotEmpty()) {
            OutlinedButton(
                onClick = {
                    selectedIds = if (
                        selectedIds.size == monthlyBills.size
                    ) {
                        emptySet()
                    } else {
                        monthlyBills.map { bill ->
                            bill.id
                        }.toSet()
                    }

                    copyDetailsIds = copyDetailsIds.intersect(selectedIds)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (
                        selectedIds.size == monthlyBills.size
                    ) {
                        "Desmarcar todas"
                    } else {
                        "Selecionar todas"
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(
                items = monthlyBills,
                key = { bill -> bill.id }
            ) { bill ->
                val isSelected = bill.id in selectedIds
                val shouldCopyDetails = bill.id in copyDetailsIds

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    selectedIds = if (checked) {
                                        selectedIds + bill.id
                                    } else {
                                        selectedIds - bill.id
                                    }

                                    if (!checked) {
                                        copyDetailsIds =
                                            copyDetailsIds - bill.id
                                    }
                                }
                            )

                            Text(
                                text = bill.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        if (isSelected) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = shouldCopyDetails,
                                    onCheckedChange = { checked ->
                                        copyDetailsIds = if (checked) {
                                            copyDetailsIds + bill.id
                                        } else {
                                            copyDetailsIds - bill.id
                                        }
                                    }
                                )

                                Text(
                                    text = "Copiar também valor e vencimento"
                                )
                            }

                            if (!shouldCopyDetails) {
                                Text(
                                    text = "Valor e vencimento ficarão pendentes.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                val selectedBills = monthlyBills.filter { bill ->
                    bill.id in selectedIds
                }

                onConfirm(
                    selectedBills,
                    copyDetailsIds
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedIds.isNotEmpty() && !isSaving
        ) {
            Text(
                text = if (isSaving) {
                    "Preparando..."
                } else {
                    "Preparar $targetMonthName"
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        ) {
            Text(text = "Cancelar")
        }
    }
}