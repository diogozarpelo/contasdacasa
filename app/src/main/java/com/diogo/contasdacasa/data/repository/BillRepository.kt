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

    suspend fun prepareNextMonth(
        sourceBills: List<Bill>,
        copyDetailsIds: Set<Long>
    ): YearMonth? {
        val firstBill = sourceBills.firstOrNull() ?: return null

        val targetMonth = YearMonth
            .of(firstBill.year, firstBill.month)
            .plusMonths(1)

        sourceBills
            .filter { bill ->
                bill.entryType == "MONTHLY"
            }
            .forEach { sourceBill ->
                val existingCopy = billDao.getCopiedBill(
                    sourceBillId = sourceBill.id,
                    month = targetMonth.monthValue,
                    year = targetMonth.year
                )

                if (existingCopy == null) {
                    val shouldCopyDetails = sourceBill.id in copyDetailsIds

                    billDao.insert(
                        Bill(
                            profileId = sourceBill.profileId,
                            name = sourceBill.name,
                            amountInCents = if (shouldCopyDetails) {
                                sourceBill.amountInCents
                            } else {
                                0
                            },
                            dueDay = if (shouldCopyDetails) {
                                sourceBill.dueDay
                            } else {
                                0
                            },
                            month = targetMonth.monthValue,
                            year = targetMonth.year,
                            requiresReview = !shouldCopyDetails,
                            entryType = "MONTHLY",
                            sourceBillId = sourceBill.id
                        )
                    )
                }
            }

        return targetMonth
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