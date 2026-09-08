package com.diogo.contasdacasa.data.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "profiles")
data class Profile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)
