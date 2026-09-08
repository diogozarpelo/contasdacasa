package com.diogo.contasdacasa.data.local

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import androidx.sqlite.driver.AndroidSQLiteDriver
import com.diogo.contasdacasa.data.model.Bill
import com.diogo.contasdacasa.data.model.Profile

@Database(
    entities = [
        Profile::class,
        Bill::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun profileDao(): ProfileDao

    abstract fun billDao(): BillDao

    companion object {

        @Volatile
        private var instance: AppDatabase? = null

        private val migration1To2 = object : Migration(1, 2) {
            override suspend fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS bills (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        profileId INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        amountInCents INTEGER NOT NULL,
                        dueDay INTEGER NOT NULL,
                        month INTEGER NOT NULL,
                        year INTEGER NOT NULL,
                        isPaid INTEGER NOT NULL,
                        isRecurring INTEGER NOT NULL,
                        FOREIGN KEY(profileId)
                            REFERENCES profiles(id)
                            ON UPDATE NO ACTION
                            ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                connection.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_bills_profileId
                    ON bills(profileId)
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder<AppDatabase>(
                    context = context.applicationContext,
                    name = "contas_da_casa.db"
                )
                    .setDriver(AndroidSQLiteDriver())
                    .addMigrations(migration1To2)
                    .build()
                    .also { database ->
                        instance = database
                    }
            }
        }
    }
}