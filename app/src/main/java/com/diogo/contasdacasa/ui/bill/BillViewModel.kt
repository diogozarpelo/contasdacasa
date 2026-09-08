package com.diogo.contasdacasa.ui.bill

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.diogo.contasdacasa.data.model.Bill
import com.diogo.contasdacasa.data.repository.BillRepository
import com.diogo.contasdacasa.ui.util.parseCurrencyToCents
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
    val wasBillUpdated: Boolean = false,
    val wasNextMonthPrepared: Boolean = false,
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
    ) {
        val profileId = uiState.profileId ?: return
        val normalizedName = name.trim()
        val amountInCents = parseCurrencyToCents(amountText)
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



    fun createInstallmentPlan(
        name: String,
        amountText: String,
        dueDayText: String,
        currentInstallmentText: String,
        totalInstallmentsText: String
    ) {
        val profileId = uiState.profileId ?: return
        val normalizedName = name.trim()
        val amountInCents = parseCurrencyToCents(amountText)
        val dueDay = dueDayText.toIntOrNull()
        val currentInstallment = currentInstallmentText.toIntOrNull()
        val totalInstallments = totalInstallmentsText.toIntOrNull()

        when {
            normalizedName.isBlank() -> {
                showError("Informe o nome do financiamento.")
                return
            }

            amountInCents == null || amountInCents <= 0 -> {
                showError("Informe um valor de parcela válido.")
                return
            }

            dueDay == null || dueDay !in 1..31 -> {
                showError("Informe um vencimento entre 1 e 31.")
                return
            }

            currentInstallment == null || currentInstallment < 1 -> {
                showError("Informe uma parcela atual válida.")
                return
            }

            totalInstallments == null ||
                totalInstallments < currentInstallment -> {
                showError("O total deve ser igual ou maior que a parcela atual.")
                return
            }

            totalInstallments > 600 -> {
                showError("O total não pode ultrapassar 600 parcelas.")
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
                repository.createInstallmentPlan(
                    profileId = profileId,
                    name = normalizedName,
                    amountInCents = amountInCents,
                    dueDay = dueDay,
                    currentInstallment = currentInstallment,
                    totalInstallments = totalInstallments,
                    startMonth = uiState.month,
                    startYear = uiState.year
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
                    errorMessage = "Não foi possível criar o financiamento."
                )
            }
        }
    }

    fun prepareNextMonth(
        selectedBills: List<Bill>,
        copyDetailsIds: Set<Long>
    ) {
        val profileId = uiState.profileId ?: return

        if (selectedBills.isEmpty()) {
            showError("Selecione pelo menos uma conta.")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(
                isSaving = true,
                wasNextMonthPrepared = false,
                errorMessage = null
            )

            try {
                val targetMonth = repository.prepareNextMonth(
                    sourceBills = selectedBills,
                    copyDetailsIds = copyDetailsIds
                ) ?: return@launch

                val bills = repository.getBills(
                    profileId = profileId,
                    month = targetMonth.monthValue,
                    year = targetMonth.year
                )

                uiState = uiState.copy(
                    month = targetMonth.monthValue,
                    year = targetMonth.year,
                    bills = bills,
                    isSaving = false,
                    wasNextMonthPrepared = true
                )
            } catch (_: Exception) {
                uiState = uiState.copy(
                    isSaving = false,
                    errorMessage = "Não foi possível preparar o próximo mês."
                )
            }
        }
    }
    fun updateBillDetails(
        bill: Bill,
        amountText: String,
        dueDayText: String
    ) {
        val amountInCents = parseCurrencyToCents(amountText)
        val dueDay = dueDayText.toIntOrNull()

        when {
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
                wasBillUpdated = false,
                errorMessage = null
            )

            try {
                repository.updateBill(
                    bill.copy(
                        amountInCents = amountInCents,
                        dueDay = dueDay,
                        requiresReview = false
                    )
                )

                val profileId = uiState.profileId ?: return@launch

                val bills = repository.getBills(
                    profileId = profileId,
                    month = uiState.month,
                    year = uiState.year
                )

                uiState = uiState.copy(
                    bills = bills,
                    isSaving = false,
                    wasBillUpdated = true
                )
            } catch (_: Exception) {
                uiState = uiState.copy(
                    isSaving = false,
                    errorMessage = "Não foi possível atualizar a conta."
                )
            }
        }
    }

    fun updateInstallmentsFromCurrent(
        bill: Bill,
        amountText: String,
        dueDayText: String
    ) {
        val amountInCents = parseCurrencyToCents(amountText)
        val dueDay = dueDayText.toIntOrNull()

        when {
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
                wasBillUpdated = false,
                errorMessage = null
            )

            try {
                repository.updateInstallmentsFromCurrent(
                    bill = bill,
                    amountInCents = amountInCents,
                    dueDay = dueDay
                )

                reloadCurrentMonthAfterChange(
                    wasBillUpdated = true
                )
            } catch (_: Exception) {
                uiState = uiState.copy(
                    isSaving = false,
                    errorMessage = "Não foi possível atualizar as parcelas."
                )
            }
        }
    }

    fun deleteInstallmentsFromCurrent(bill: Bill) {
        viewModelScope.launch {
            try {
                repository.deleteInstallmentsFromCurrent(bill)
                loadBills()
            } catch (_: Exception) {
                showError("Não foi possível excluir as parcelas.")
            }
        }
    }

    private suspend fun reloadCurrentMonthAfterChange(
        wasBillUpdated: Boolean
    ) {
        val profileId = uiState.profileId ?: return

        val bills = repository.getBills(
            profileId = profileId,
            month = uiState.month,
            year = uiState.year
        )

        uiState = uiState.copy(
            bills = bills,
            isSaving = false,
            wasBillUpdated = wasBillUpdated
        )
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
                repository.deleteBillFromMonth(bill)
                loadBills()
            } catch (_: Exception) {
                showError("Não foi possível excluir a conta.")
            }
        }
    }


    fun clearFeedback() {
        uiState = uiState.copy(
            wasBillCreated = false,
            wasBillUpdated = false,
            wasNextMonthPrepared = false,
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