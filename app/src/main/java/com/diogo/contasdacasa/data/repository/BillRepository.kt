package com.diogo.contasdacasa.data.repository

import com.diogo.contasdacasa.data.local.BillDao
import com.diogo.contasdacasa.data.model.Bill

class BillRepository(
    private val billDao: BillDao
) {

    suspend fun createBill(bill: Bill): Long {
        return billDao.insert(bill)
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

    suspend fun deleteBill(bill: Bill) {
        billDao.delete(bill)
    }
}