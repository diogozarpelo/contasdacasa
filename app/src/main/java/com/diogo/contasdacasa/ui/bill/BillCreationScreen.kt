package com.diogo.contasdacasa.ui.bill

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.diogo.contasdacasa.ui.util.formatMonthName

@Composable
fun BillCreationScreen(
    uiState: BillUiState,
    onCreateMonthlyBill: (String, String, String) -> Unit,
    onCreateInstallmentPlan: (
        String,
        String,
        String,
        String,
        String
    ) -> Unit,
    onClearFeedback: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var entryType by rememberSaveable {
        mutableStateOf("MONTHLY")
    }

    var name by rememberSaveable {
        mutableStateOf("")
    }

    var amount by rememberSaveable {
        mutableStateOf("")
    }

    var dueDay by rememberSaveable {
        mutableStateOf("")
    }

    var currentInstallment by rememberSaveable {
        mutableStateOf("")
    }

    var totalInstallments by rememberSaveable {
        mutableStateOf("")
    }

    val isInstallment = entryType == "INSTALLMENT"

    val monthName = formatMonthName(uiState.month)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Novo lançamento",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "$monthName de ${uiState.year}",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Tipo de lançamento",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    entryType = "MONTHLY"
                    onClearFeedback()
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = !isInstallment,
                onClick = {
                    entryType = "MONTHLY"
                    onClearFeedback()
                }
            )

            Text(text = "Conta mensal")
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    entryType = "INSTALLMENT"
                    onClearFeedback()
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isInstallment,
                onClick = {
                    entryType = "INSTALLMENT"
                    onClearFeedback()
                }
            )

            Text(text = "Empréstimo ou financiamento")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                onClearFeedback()
            },
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
            enabled = !uiState.isSaving
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = {
                amount = it
                onClearFeedback()
            },
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
            enabled = !uiState.isSaving
        )

        Spacer(modifier = Modifier.height(10.dp))

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

        if (isInstallment) {
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = currentInstallment,
                onValueChange = { value ->
                    currentInstallment = value.filter { character ->
                        character.isDigit()
                    }.take(3)

                    onClearFeedback()
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
                enabled = !uiState.isSaving
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = totalInstallments,
                onValueChange = { value ->
                    totalInstallments = value.filter { character ->
                        character.isDigit()
                    }.take(3)

                    onClearFeedback()
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
                enabled = !uiState.isSaving
            )
        }

        uiState.errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (isInstallment) {
                    onCreateInstallmentPlan(
                        name,
                        amount,
                        dueDay,
                        currentInstallment,
                        totalInstallments
                    )
                } else {
                    onCreateMonthlyBill(
                        name,
                        amount,
                        dueDay
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSaving
        ) {
            Text(
                text = if (uiState.isSaving) {
                    "Salvando..."
                } else if (isInstallment) {
                    "Criar parcelas"
                } else {
                    "Salvar conta"
                }
            )
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