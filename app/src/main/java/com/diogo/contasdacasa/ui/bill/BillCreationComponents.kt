package com.diogo.contasdacasa.ui.bill

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
internal fun BillEntryTypeSelector(
    isInstallment: Boolean,
    onMonthlySelected: () -> Unit,
    onInstallmentSelected: () -> Unit
) {
    Text(
        text = "Tipo de lançamento",
        style = MaterialTheme.typography.titleMedium
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onMonthlySelected),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = !isInstallment,
            onClick = onMonthlySelected
        )

        Text(text = "Conta mensal")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onInstallmentSelected),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isInstallment,
            onClick = onInstallmentSelected
        )

        Text(text = "Empréstimo ou financiamento")
    }
}

@Composable
internal fun BillCreationFields(
    isInstallment: Boolean,
    name: String,
    amount: String,
    dueDay: String,
    currentInstallment: String,
    totalInstallments: String,
    isSaving: Boolean,
    onNameChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onDueDayChange: (String) -> Unit,
    onCurrentInstallmentChange: (String) -> Unit,
    onTotalInstallmentsChange: (String) -> Unit
) {
    Column {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(
                    text = if (isInstallment) {
                        "Nome do financiamento"
                    } else {
                        "Nome da conta"
                    }
                )
            },
            singleLine = true,
            enabled = !isSaving
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = onAmountChange,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(
                    text = if (isInstallment) {
                        "Valor da parcela (R$)"
                    } else {
                        "Valor (R$)"
                    }
                )
            },
            placeholder = {
                Text(text = "0,00")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            ),
            singleLine = true,
            enabled = !isSaving
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = dueDay,
            onValueChange = { value ->
                onDueDayChange(
                    value
                        .filter { character -> character.isDigit() }
                        .take(2)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(text = "Dia do vencimento")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            singleLine = true,
            enabled = !isSaving
        )

        if (isInstallment) {
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = currentInstallment,
                onValueChange = { value ->
                    onCurrentInstallmentChange(
                        value
                            .filter { character -> character.isDigit() }
                            .take(3)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = "Parcela atual")
                },
                placeholder = {
                    Text(text = "Ex.: 4")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true,
                enabled = !isSaving
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = totalInstallments,
                onValueChange = { value ->
                    onTotalInstallmentsChange(
                        value
                            .filter { character -> character.isDigit() }
                            .take(3)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = "Total de parcelas")
                },
                placeholder = {
                    Text(text = "Ex.: 12")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true,
                enabled = !isSaving
            )
        }
    }
}