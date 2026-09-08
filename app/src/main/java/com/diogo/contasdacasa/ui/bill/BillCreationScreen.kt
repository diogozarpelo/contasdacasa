package com.diogo.contasdacasa.ui.bill

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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

        BillEntryTypeSelector(
            isInstallment = isInstallment,
            onMonthlySelected = {
                entryType = "MONTHLY"
                onClearFeedback()
            },
            onInstallmentSelected = {
                entryType = "INSTALLMENT"
                onClearFeedback()
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        BillCreationFields(
            isInstallment = isInstallment,
            name = name,
            amount = amount,
            dueDay = dueDay,
            currentInstallment = currentInstallment,
            totalInstallments = totalInstallments,
            isSaving = uiState.isSaving,
            onNameChange = { value ->
                name = value
                onClearFeedback()
            },
            onAmountChange = { value ->
                amount = value
                onClearFeedback()
            },
            onDueDayChange = { value ->
                dueDay = value
                onClearFeedback()
            },
            onCurrentInstallmentChange = { value ->
                currentInstallment = value
                onClearFeedback()
            },
            onTotalInstallmentsChange = { value ->
                totalInstallments = value
                onClearFeedback()
            }
        )
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