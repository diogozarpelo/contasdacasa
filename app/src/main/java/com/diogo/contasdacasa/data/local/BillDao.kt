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
          AND isExcluded = 0
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
        WHERE recurringSeriesId = :seriesId
          AND month = :month
          AND year = :year
        LIMIT 1
        """
    )
    suspend fun getBySeriesAndMonth(
        seriesId: Long,
        month: Int,
        year: Int
    ): Bill?

    @Query("UPDATE bills SET isPaid = :isPaid WHERE id = :id")
    suspend fun updatePaidStatus(
        id: Long,
        isPaid: Boolean
    )

    @Query("UPDATE bills SET isExcluded = 1 WHERE id = :id")
    suspend fun excludeFromMonth(id: Long)

    @Query(
        """
        DELETE FROM bills
        WHERE recurringSeriesId = :seriesId
          AND (
              year > :year
              OR (year = :year AND month > :month)
          )
        """
    )
    suspend fun deleteFutureInstances(
        seriesId: Long,
        month: Int,
        year: Int
    )
}