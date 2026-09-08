package com.diogo.contasdacasa.data.model

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "recurring_bill_series",
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["profileId"])
    ]
)
data class RecurringBillSeries(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val name: String,
    val amountInCents: Long,
    val dueDay: Int,
    val startMonth: Int,
    val startYear: Int,
    val endMonth: Int? = null,
    val endYear: Int? = null
)