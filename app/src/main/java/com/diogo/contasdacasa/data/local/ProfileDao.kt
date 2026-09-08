package com.diogo.contasdacasa.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import com.diogo.contasdacasa.data.model.Profile

@Dao
interface ProfileDao {

    @Insert
    suspend fun insert(profile: Profile): Long

    @Query("SELECT * FROM profiles ORDER BY name COLLATE NOCASE ASC")
    suspend fun getAll(): List<Profile>

    @Query("SELECT * FROM profiles WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Profile?

    @Query("UPDATE profiles SET name = :name WHERE id = :id")
    suspend fun updateName(
        id: Long,
        name: String
    )

    @Query("DELETE FROM profiles WHERE id = :id")
    suspend fun deleteById(id: Long)
}