package com.diogo.contasdacasa.ui.bill

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.diogo.contasdacasa.data.model.Bill
import com.diogo.contasdacasa.data.repository.BillRepository
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.YearMonth
import kotlinx.coroutines.launch

data class BillUiState(
    val profileId: Long? = null,
    val month: Int = YearMonth.now().monthValue,
    val year: Int = YearMonth.now().year,
    val bills: List<Bill> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val wasBillCreated: Boolean = false,
    val errorMessage: String? = null
)

class BillViewModel(
    private val repository: BillRepository
) : ViewModel() {

    var uiState by mutableStateOf(BillUiState())
        private set

    fun openProfile(profileId: Long) {
        if (uiState.profileId == profileId) {
            return
        }

        val currentMonth = YearMonth.now()

        uiState = BillUiState(
            profileId = profileId,
            month = currentMonth.monthValue,
            year = currentMonth.year
        )

        loadBills()
    }

    fun changeMonth(offset: Long) {
        val profileId = uiState.profileId ?: return

        val newMonth = YearMonth
            .of(uiState.year, uiState.month)
            .plusMonths(offset)

        uiState = uiState.copy(
            profileId = profileId,
            month = newMonth.monthValue,
            year = newMonth.year,
            wasBillCreated = false,
            errorMessage = null
        )

        loadBills()
    }

    fun createBill(
        name: String,
        amountText: String,
        dueDayText: String,
        isRecurring: Boolean
    ) {
        val profileId = uiState.profileId ?: return
        val normalizedName = name.trim()
        val amountInCents = parseAmountInCents(amountText)
        val dueDay = dueDayText.toIntOrNull()

        when {
            normalizedName.isBlank() -> {
                showError("Informe o nome da conta.")
                return
            }

            amountInCents == null || amountInCents <= 0 -> {
                showError("Informe um valor válido.")
                return
            }

            dueDay == null || dueDay !in 1..31 -> {
                showError("Informe um vencimento entre 1 e 31.")
                return
            }
        }

        viewModelScope.launch {
            uiState = uiState.copy(
                isSaving = true,
                wasBillCreated = false,
                errorMessage = null
            )

            try {
                repository.createBill(
                    Bill(
                        profileId = profileId,
                        name = normalizedName,
                        amountInCents = amountInCents,
                        dueDay = dueDay,
                        month = uiState.month,
                        year = uiState.year,
                        isRecurring = isRecurring
                    )
                )

                val bills = repository.getBills(
                    profileId = profileId,
                    month = uiState.month,
                    year = uiState.year
                )

                uiState = uiState.copy(
                    bills = bills,
                    isSaving = false,
                    wasBillCreated = true
                )
            } catch (_: Exception) {
                uiState = uiState.copy(
                    isSaving = false,
                    errorMessage = "Não foi possível cadastrar a conta."
                )
            }
        }
    }

    fun togglePaid(bill: Bill) {
        viewModelScope.launch {
            try {
                repository.updatePaidStatus(
                    id = bill.id,
                    isPaid = !bill.isPaid
                )

                loadBills()
            } catch (_: Exception) {
                showError("Não foi possível atualizar a conta.")
            }
        }
    }

    fun deleteBill(bill: Bill) {
        viewModelScope.launch {
            try {
                repository.deleteBill(bill)
                loadBills()
            } catch (_: Exception) {
                showError("Não foi possível excluir a conta.")
            }
        }
    }

    fun clearFeedback() {
        uiState = uiState.copy(
            wasBillCreated = false,
            errorMessage = null
        )
    }

    private fun loadBills() {
        val profileId = uiState.profileId ?: return
        val month = uiState.month
        val year = uiState.year

        viewModelScope.launch {
            uiState = uiState.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val bills = repository.getBills(
                    profileId = profileId,
                    month = month,
                    year = year
                )

                uiState = uiState.copy(
                    bills = bills,
                    isLoading = false
                )
            } catch (_: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Não foi possível carregar as contas."
                )
            }
        }
    }

    private fun showError(message: String) {
        uiState = uiState.copy(
            wasBillCreated = false,
            errorMessage = message
        )
    }

    private fun parseAmountInCents(value: String): Long? {
        return try {
            val cleanedValue = value
                .trim()
                .replace("R$", "")
                .replace(" ", "")

            val normalizedValue = if (cleanedValue.contains(",")) {
                cleanedValue
                    .replace(".", "")
                    .replace(",", ".")
            } else {
                cleanedValue
            }

            BigDecimal(normalizedValue)
                .setScale(2, RoundingMode.HALF_UP)
                .movePointRight(2)
                .longValueExact()
        } catch (_: Exception) {
            null
        }
    }

    class Factory(
        private val repository: BillRepository
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BillViewModel::class.java)) {
                return BillViewModel(repository) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}