package com.diogo.contasdacasa.ui.bill

object BillInputValidator {

    fun validateMonthlyBill(
        name: String,
        amountInCents: Long?,
        dueDay: Int?
    ): String? {
        return when {
            name.isBlank() ->
                "Informe o nome da conta."

            amountInCents == null || amountInCents <= 0 ->
                "Informe um valor válido."

            dueDay == null || dueDay !in 1..31 ->
                "Informe um vencimento entre 1 e 31."

            else -> null
        }
    }

    fun validateInstallmentPlan(
        name: String,
        amountInCents: Long?,
        dueDay: Int?,
        currentInstallment: Int?,
        totalInstallments: Int?
    ): String? {
        return when {
            name.isBlank() ->
                "Informe o nome do financiamento."

            amountInCents == null || amountInCents <= 0 ->
                "Informe um valor de parcela válido."

            dueDay == null || dueDay !in 1..31 ->
                "Informe um vencimento entre 1 e 31."

            currentInstallment == null || currentInstallment < 1 ->
                "Informe uma parcela atual válida."

            totalInstallments == null ||
                totalInstallments < currentInstallment ->
                "O total deve ser igual ou maior que a parcela atual."

            totalInstallments > 600 ->
                "O total não pode ultrapassar 600 parcelas."

            else -> null
        }
    }
}