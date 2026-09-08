package com.diogo.contasdacasa.data.local

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.AndroidSQLiteDriver
import com.diogo.contasdacasa.data.model.Profile

@Database(
    entities = [Profile::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun profileDao(): ProfileDao

    companion object {

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder<AppDatabase>(
                    context = context.applicationContext,
                    name = "contas_da_casa.db"
                )
                    .setDriver(AndroidSQLiteDriver())
                    .build()
                    .also { database ->
                        instance = database
                    }
            }
        }
    }
}
