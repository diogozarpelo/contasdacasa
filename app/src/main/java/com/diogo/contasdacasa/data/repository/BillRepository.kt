package com.diogo.contasdacasa.data.repository

import com.diogo.contasdacasa.data.local.BillDao
import com.diogo.contasdacasa.data.model.Bill
import java.time.YearMonth
import java.util.UUID

class BillRepository(
    private val billDao: BillDao
) {

    suspend fun createBill(bill: Bill): Long {
        return billDao.insert(bill)
    }

    suspend fun createInstallmentPlan(
        profileId: Long,
        name: String,
        amountInCents: Long,
        dueDay: Int,
        currentInstallment: Int,
        totalInstallments: Int,
        startMonth: Int,
        startYear: Int
    ) {
        val groupId = UUID.randomUUID().toString()
        val initialMonth = YearMonth.of(startYear, startMonth)

        val installments = (currentInstallment..totalInstallments).map { number ->
            val installmentMonth = initialMonth.plusMonths(
                (number - currentInstallment).toLong()
            )

            Bill(
                profileId = profileId,
                name = name,
                amountInCents = amountInCents,
                dueDay = dueDay,
                month = installmentMonth.monthValue,
                year = installmentMonth.year,
                entryType = "INSTALLMENT",
                installmentGroupId = groupId,
                installmentNumber = number,
                totalInstallments = totalInstallments
            )
        }

        billDao.insertAll(installments)
    }

    suspend fun getBills(
        profileId: Long,
        month: Int,
        year: Int
    ): List<Bill> {
        return billDao.getByProfileAndMonth(
            profileId = profileId,
            month = month,
            year = year
        )
    }

    suspend fun updateBill(bill: Bill) {
        billDao.update(bill)
    }

    suspend fun updatePaidStatus(
        id: Long,
        isPaid: Boolean
    ) {
        billDao.updatePaidStatus(
            id = id,
            isPaid = isPaid
        )
    }

    suspend fun deleteBillFromMonth(bill: Bill) {
        billDao.delete(bill)
    }
}