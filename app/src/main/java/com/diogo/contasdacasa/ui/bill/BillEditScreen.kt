package com.diogo.contasdacasa.ui.bill

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.diogo.contasdacasa.data.model.Bill
import java.math.BigDecimal

@Composable
fun BillEditScreen(
    bill: Bill,
    uiState: BillUiState,
    onSave: (Bill, String, String) -> Unit,
    onSaveFromCurrent: (Bill, String, String) -> Unit,
    onClearFeedback: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var amount by rememberSaveable(bill.id) {
        mutableStateOf(
            if (bill.amountInCents == 0L) {
                ""
            } else {
                BigDecimal(bill.amountInCents)
                    .movePointLeft(2)
                    .toPlainString()
                    .replace(".", ",")
            }
        )
    }

    var dueDay by rememberSaveable(bill.id) {
        mutableStateOf(
            if (bill.dueDay == 0) {
                ""
            } else {
                bill.dueDay.toString()
            }
        )
    }

    val isInstallment = bill.entryType == "INSTALLMENT"

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Editar lançamento",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = bill.name,
            style = MaterialTheme.typography.titleLarge
        )

        if (
            isInstallment &&
            bill.installmentNumber != null &&
            bill.totalInstallments != null
        ) {
            Text(
                text = "Parcela ${bill.installmentNumber} de ${bill.totalInstallments}",
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = {
                amount = it
                onClearFeedback()
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(text = "Valor (R$)")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            ),
            singleLine = true,
            enabled = !uiState.isSaving
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = dueDay,
            onValueChange = { value ->
                dueDay = value.filter { character ->
                    character.isDigit()
                }.take(2)

                onClearFeedback()
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(text = "Dia do vencimento")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            singleLine = true,
            enabled = !uiState.isSaving
        )

        uiState.errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onSave(
                    bill,
                    amount,
                    dueDay
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSaving
        ) {
            Text(
                text = if (isInstallment) {
                    "Salvar somente esta parcela"
                } else {
                    "Salvar alterações"
                }
            )
        }

        if (isInstallment) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    onSaveFromCurrent(
                        bill,
                        amount,
                        dueDay
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                Text(text = "Salvar nesta e nas próximas")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSaving
        ) {
            Text(text = "Cancelar")
        }
    }
}