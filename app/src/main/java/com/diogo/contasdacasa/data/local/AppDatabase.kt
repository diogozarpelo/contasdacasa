package com.diogo.contasdacasa.data.local

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.sqlite.execSQL
import com.diogo.contasdacasa.data.model.Bill
import com.diogo.contasdacasa.data.model.Profile

@Database(
    entities = [
        Profile::class,
        Bill::class
    ],
    version = 6,
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

        private val migration2To3 = object : Migration(2, 3) {
            override suspend fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS recurring_bill_series (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        profileId INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        amountInCents INTEGER NOT NULL,
                        dueDay INTEGER NOT NULL,
                        startMonth INTEGER NOT NULL,
                        startYear INTEGER NOT NULL,
                        endMonth INTEGER,
                        endYear INTEGER,
                        FOREIGN KEY(profileId)
                            REFERENCES profiles(id)
                            ON UPDATE NO ACTION
                            ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                connection.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_recurring_bill_series_profileId
                    ON recurring_bill_series(profileId)
                    """.trimIndent()
                )

                connection.execSQL(
                    "ALTER TABLE bills ADD COLUMN recurringSeriesId INTEGER"
                )

                connection.execSQL(
                    "ALTER TABLE bills ADD COLUMN isExcluded INTEGER NOT NULL DEFAULT 0"
                )

                connection.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_bills_recurringSeriesId
                    ON bills(recurringSeriesId)
                    """.trimIndent()
                )

                connection.execSQL(
                    """
                    INSERT INTO recurring_bill_series (
                        id,
                        profileId,
                        name,
                        amountInCents,
                        dueDay,
                        startMonth,
                        startYear,
                        endMonth,
                        endYear
                    )
                    SELECT
                        id,
                        profileId,
                        name,
                        amountInCents,
                        dueDay,
                        month,
                        year,
                        NULL,
                        NULL
                    FROM bills
                    WHERE isRecurring = 1
                    """.trimIndent()
                )

                connection.execSQL(
                    """
                    UPDATE bills
                    SET recurringSeriesId = id
                    WHERE isRecurring = 1
                    """.trimIndent()
                )
            }
        }


        private val migration3To4 = object : Migration(3, 4) {
            override suspend fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    "ALTER TABLE bills ADD COLUMN requiresReview INTEGER NOT NULL DEFAULT 0"
                )

                connection.execSQL(
                    """
                    UPDATE bills
                    SET amountInCents = 0,
                        dueDay = 0,
                        requiresReview = 1
                    WHERE recurringSeriesId IS NOT NULL
                      AND EXISTS (
                          SELECT 1
                          FROM recurring_bill_series
                          WHERE recurring_bill_series.id = bills.recurringSeriesId
                            AND (
                                bills.year > recurring_bill_series.startYear
                                OR (
                                    bills.year = recurring_bill_series.startYear
                                    AND bills.month > recurring_bill_series.startMonth
                                )
                            )
                      )
                    """.trimIndent()
                )
            }
        }

        private val migration4To5 = object : Migration(4, 5) {
            override suspend fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    "ALTER TABLE bills ADD COLUMN entryType TEXT NOT NULL DEFAULT 'MONTHLY'"
                )

                connection.execSQL(
                    "ALTER TABLE bills ADD COLUMN installmentGroupId TEXT"
                )

                connection.execSQL(
                    "ALTER TABLE bills ADD COLUMN installmentNumber INTEGER"
                )

                connection.execSQL(
                    "ALTER TABLE bills ADD COLUMN totalInstallments INTEGER"
                )

                connection.execSQL(
                    "ALTER TABLE bills ADD COLUMN sourceBillId INTEGER"
                )

                connection.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_bills_installmentGroupId
                    ON bills(installmentGroupId)
                    """.trimIndent()
                )

                connection.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_bills_sourceBillId
                    ON bills(sourceBillId)
                    """.trimIndent()
                )
            }
        }

        private val migration5To6 = object : Migration(5, 6) {
            override suspend fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    """
                    CREATE TABLE bills_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        profileId INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        amountInCents INTEGER NOT NULL,
                        dueDay INTEGER NOT NULL,
                        month INTEGER NOT NULL,
                        year INTEGER NOT NULL,
                        isPaid INTEGER NOT NULL,
                        requiresReview INTEGER NOT NULL DEFAULT 0,
                        entryType TEXT NOT NULL DEFAULT 'MONTHLY',
                        installmentGroupId TEXT,
                        installmentNumber INTEGER,
                        totalInstallments INTEGER,
                        sourceBillId INTEGER,
                        FOREIGN KEY(profileId)
                            REFERENCES profiles(id)
                            ON UPDATE NO ACTION
                            ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                connection.execSQL(
                    """
                    INSERT INTO bills_new (
                        id,
                        profileId,
                        name,
                        amountInCents,
                        dueDay,
                        month,
                        year,
                        isPaid,
                        requiresReview,
                        entryType,
                        installmentGroupId,
                        installmentNumber,
                        totalInstallments,
                        sourceBillId
                    )
                    SELECT
                        id,
                        profileId,
                        name,
                        amountInCents,
                        dueDay,
                        month,
                        year,
                        isPaid,
                        requiresReview,
                        entryType,
                        installmentGroupId,
                        installmentNumber,
                        totalInstallments,
                        sourceBillId
                    FROM bills
                    WHERE isExcluded = 0
                    """.trimIndent()
                )

                connection.execSQL("DROP TABLE bills")
                connection.execSQL("ALTER TABLE bills_new RENAME TO bills")

                connection.execSQL(
                    "CREATE INDEX index_bills_profileId ON bills(profileId)"
                )

                connection.execSQL(
                    """
                    CREATE INDEX index_bills_installmentGroupId
                    ON bills(installmentGroupId)
                    """.trimIndent()
                )

                connection.execSQL(
                    """
                    CREATE INDEX index_bills_sourceBillId
                    ON bills(sourceBillId)
                    """.trimIndent()
                )

                connection.execSQL(
                    "DROP TABLE IF EXISTS recurring_bill_series"
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
                    .addMigrations(
                        migration1To2,
                        migration2To3,
                        migration3To4,
                        migration4To5,
                        migration5To6
                    )
                    .build()
                    .also { database ->
                        instance = database
                    }
            }
        }
    }
}