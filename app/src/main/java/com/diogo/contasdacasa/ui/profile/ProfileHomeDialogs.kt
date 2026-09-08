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
internal fun DeleteBillDialog(
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
                text = if (bill.entryType == BillEntryType.INSTALLMENT) {
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
                        text = if (bill.entryType == BillEntryType.INSTALLMENT) {
                            "Excluir somente esta parcela"
                        } else {
                            "Excluir"
                        },
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (bill.entryType == BillEntryType.INSTALLMENT) {
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
