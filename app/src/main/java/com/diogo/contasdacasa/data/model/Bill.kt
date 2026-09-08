package com.diogo.contasdacasa.data.model

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "bills",
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["profileId"]),
        Index(value = ["installmentGroupId"]),
        Index(value = ["sourceBillId"])
    ]
)
data class Bill(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val name: String,
    val amountInCents: Long,
    val dueDay: Int,
    val month: Int,
    val year: Int,
    val isPaid: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val requiresReview: Boolean = false,
    @ColumnInfo(defaultValue = "'MONTHLY'")
    val entryType: String = "MONTHLY",
    val installmentGroupId: String? = null,
    val installmentNumber: Int? = null,
    val totalInstallments: Int? = null,
    val sourceBillId: Long? = null
)