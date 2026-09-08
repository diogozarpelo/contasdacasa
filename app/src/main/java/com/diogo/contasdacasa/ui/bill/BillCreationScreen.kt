package com.diogo.contasdacasa.ui.bill

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun BillCreationScreen(
    uiState: BillUiState,
    onCreateBill: (String, String, String, Boolean) -> Unit,
    onClearFeedback: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by rememberSaveable {
        mutableStateOf("")
    }

    var amount by rememberSaveable {
        mutableStateOf("")
    }

    var dueDay by rememberSaveable {
        mutableStateOf("")
    }

    var isRecurring by rememberSaveable {
        mutableStateOf(false)
    }

    val monthName = Month
        .of(uiState.month)
        .getDisplayName(TextStyle.FULL, Locale("pt", "BR"))
        .replaceFirstChar { character ->
            character.uppercase()
        }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Nova conta",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "$monthName de ${uiState.year}",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                onClearFeedback()
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(text = "Nome da conta")
            },
            singleLine = true,
            enabled = !uiState.isSaving
        )

        Spacer(modifier = Modifier.height(12.dp))

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
            placeholder = {
                Text(text = "0,00")
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

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isRecurring,
                onCheckedChange = {
                    isRecurring = it
                    onClearFeedback()
                },
                enabled = !uiState.isSaving
            )

            Text(text = "Repetir esta conta nos próximos meses")
        }

        uiState.errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                onCreateBill(
                    name,
                    amount,
                    dueDay,
                    isRecurring
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSaving
        ) {
            Text(
                text = if (uiState.isSaving) {
                    "Salvando..."
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