package com.diogo.contasdacasa.ui.bill

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.diogo.contasdacasa.data.model.Bill

@Composable
internal fun SelectAllBillsButton(
    allSelected: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = if (allSelected) {
                "Desmarcar todas"
            } else {
                "Selecionar todas"
            }
        )
    }
}

@Composable
internal fun PrepareNextMonthBillCard(
    bill: Bill,
    isSelected: Boolean,
    shouldCopyDetails: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    onCopyDetailsChange: (Boolean) -> Unit
) {
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
                    onCheckedChange = onSelectedChange
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
                        onCheckedChange = onCopyDetailsChange
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