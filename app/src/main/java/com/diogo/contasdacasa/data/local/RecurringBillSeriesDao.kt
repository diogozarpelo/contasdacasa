package com.diogo.contasdacasa.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import com.diogo.contasdacasa.data.model.RecurringBillSeries

@Dao
interface RecurringBillSeriesDao {

    @Insert
    suspend fun insert(series: RecurringBillSeries): Long

    @Query(
        """
        SELECT * FROM recurring_bill_series
        WHERE profileId = :profileId
          AND (
              startYear < :year
              OR (startYear = :year AND startMonth <= :month)
          )
          AND (
              endYear IS NULL
              OR endYear > :year
              OR (endYear = :year AND endMonth >= :month)
          )
        """
    )
    suspend fun getActiveForMonth(
        profileId: Long,
        month: Int,
        year: Int
    ): List<RecurringBillSeries>

    @Query(
        """
        UPDATE recurring_bill_series
        SET endMonth = :endMonth,
            endYear = :endYear
        WHERE id = :seriesId
        """
    )
    suspend fun stopSeries(
        seriesId: Long,
        endMonth: Int,
        endYear: Int
    )
}