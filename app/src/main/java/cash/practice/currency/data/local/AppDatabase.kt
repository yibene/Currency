package cash.practice.currency.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import cash.practice.currency.model.Rate

@Database(
    entities = [
        Rate::class
    ],
    version = 2
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun currencyDao(): CurrencyDao
}