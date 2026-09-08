package com.diogo.contasdacasa.data.repository

import com.diogo.contasdacasa.data.model.BillEntryType

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
                entryType = BillEntryType.INSTALLMENT,
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
                bill.entryType == BillEntryType.MONTHLY
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
                            entryType = BillEntryType.MONTHLY,
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


    suspend fun updateInstallmentsFromCurrent(
        bill: Bill,
        amountInCents: Long,
        dueDay: Int
    ) {
        val groupId = bill.installmentGroupId ?: return
        val installmentNumber = bill.installmentNumber ?: return

        billDao.updateInstallmentsFrom(
            groupId = groupId,
            fromInstallment = installmentNumber,
            amountInCents = amountInCents,
            dueDay = dueDay
        )
    }

    suspend fun deleteInstallmentsFromCurrent(bill: Bill) {
        val groupId = bill.installmentGroupId ?: return
        val installmentNumber = bill.installmentNumber ?: return

        billDao.deleteInstallmentsFrom(
            groupId = groupId,
            fromInstallment = installmentNumber
        )
    }
    suspend fun deleteBillFromMonth(bill: Bill) {
        billDao.delete(bill)
    }
}