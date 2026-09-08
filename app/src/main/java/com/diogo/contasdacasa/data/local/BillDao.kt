package com.diogo.contasdacasa.data.local

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import com.diogo.contasdacasa.data.model.Bill

@Dao
interface BillDao {

    @Insert
    suspend fun insert(bill: Bill): Long

    @Insert
    suspend fun insertAll(bills: List<Bill>): List<Long>

    @Update
    suspend fun update(bill: Bill)

    @Delete
    suspend fun delete(bill: Bill)

    @Query(
        """
        SELECT * FROM bills
        WHERE profileId = :profileId
          AND month = :month
          AND year = :year
        ORDER BY isPaid ASC, dueDay ASC, name COLLATE NOCASE ASC
        """
    )
    suspend fun getByProfileAndMonth(
        profileId: Long,
        month: Int,
        year: Int
    ): List<Bill>

    @Query("SELECT * FROM bills WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Bill?

    @Query(
        """
        SELECT * FROM bills
        WHERE sourceBillId = :sourceBillId
          AND month = :month
          AND year = :year
        LIMIT 1
        """
    )
    suspend fun getCopiedBill(
        sourceBillId: Long,
        month: Int,
        year: Int
    ): Bill?

    @Query("UPDATE bills SET isPaid = :isPaid WHERE id = :id")
    suspend fun updatePaidStatus(
        id: Long,
        isPaid: Boolean
    )
}